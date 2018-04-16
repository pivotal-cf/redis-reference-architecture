# Multiple Redis Cache Failure Handling Example

This sample Spring App demonstrates using two Redis servers as a highly-available cache for a time-intensive method.
This could include lookup in another data service, or another slow process such as page generation. This architecture is specifically suited to the case where an App cannot handle the downtime caused by routine Redis service upgrades.

* Appropriate if you have a method that will benefit from caching:
  - the method is slow to run
  - the method always returns the same result for a given input (similar to a pure function, but may have idempotent side effects)
  - the method is called repeatedly with the same input
* Appropriate if having one cache available while another is down is important to your App
* Not appropriate if you require direct access and manipulation of the underlying Redis cache

---

The sample app takes requests at `/token?id=<something>` and returns a token.
This token is cached in both Redis servers and fetched from the primary Redis server by default.
If there is a a cache miss in the primary Redis server or that server is down, the token will be fetched from the secondary Redis server.
Finally, if neither server contains the token or neither server is up, then the token will be generated anew and written to the caches if possible.

![Process Diagram](/assets/multi-redis-diagram.svg "Process Diagram")


### Configuring Redis Parallel Upgrades
The Redis for PCF tile v1.13+ makes it possible to upgrade On-Demand Redis service instances in parallel. This is configured through the `max-in-flight` and `canaries` settings. Prior to 1.13, all service instances are upgraded one at a time. If your tile is configured to upgrade in parallel, it is possible that the two Redis services bound to your App are upgraded at the same time. This would make the cache completely unavailable.


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
