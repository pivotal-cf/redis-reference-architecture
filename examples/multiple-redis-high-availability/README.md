# Multiple Redis Cache Failure Handling Example

This sample Spring App demonstrates using two Redis servers as a highly-available cache for a time-intensive method.
This could include lookup in another data service, or another slow process such as page generation.

The sample app takes requests at `/token?id=<something>` and returns a token.
This token is cached in both Redis servers and fetched from the primary Redis server by default.
If there is a a cache miss in the primary Redis server or that server is down, the token will be fetched from the secondary Redis server.
Finally, if neither server contains the token or neither server is up, then the token will be generated anew and written to the caches if possible.

![Process Diagram](/assets/multi-redis-diagram.svg "Process Diagram")

## Run locally
Start redis-servers with the ports corresponding to the ports defined in [application.properties](src/main/resources/application.properties).
If you desire different ports or hosts, simply update your `application.properties` to correspond to your desired setup.

Start the first local redis-server:
```
redis-server --port <port1>
```

Start the second local redis-server:
```
redis-server --port <port2>
```
Then start the Spring app:
```
cd examples/multiple-redis-high-availability
gradle bootRun
```

Visit in your browser:
```
http://localhost:8080/token?id=<ANY_ID>
```
Observe that the `duration` field of the response is long the first time each token is passed.
This decreases drastically with subsequent calls when the Redis cache is used.
Terminate the primary Redis cache process and see that it still follows the previous `duration` behaviour.
Repeat this with both Redis cache processes down and see that all queries result in a long `duration` field.


## Run on CF
Build the project:
```
gradle build
```
Push the app, non-started, by providing the path to the app jar file:
```
cf push --no-start <APP_NAME> -p build/libs/multiple-redis-high-availability*.jar
```
This will print the route to the App, usually '<APP_NAME>.apps.<CF_URL>.com'.


Create two Redis services. Spring auto-configures Redis connection using the service-binding so it should just work.
```
#cf create-service <REDIS_SERVICE> <SERVICE_PLAN> <SERVICE_INSTANCE_NAME>
 cf create-service  p.redis         cache-small   <SERVICE_INSTANCE_NAME_1>
 cf create-service  p.redis         cache-small   <SERVICE_INSTANCE_NAME_2>
```

Bind your Redis services, the primary cache will be the first service bound to the app, and start the App:
```
cf bind-service <APP_NAME> <SERVICE_INSTANCE_NAME_1>
cf bind-service <APP_NAME> <SERVICE_INSTANCE_NAME_2>
cf start <APP_NAME>
```

Reference: https://docs.cloudfoundry.org/buildpacks/java/configuring-service-connections/spring-service-bindings.html#redis

## Errors
* `RedisCacheConfig` and `RedisCacheError` intercept the errors when Redis is missing. This allows the request to fall through to the expensive token generation call.
