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

## Run on CF
Build the project:
```
gradle build
```
Push the app, non-started, by providing the path to the app jar file:
```
cf push --no-start
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
* `RedisCacheConfig` intercepts the errors when Redis is missing. This allows the request to fall through to the expensive token generation call.
