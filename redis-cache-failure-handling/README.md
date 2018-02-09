# Cache Failure Handling Example

Sample app which takes request at `/token?id=<something>` and returns a token.
When Redis is missing or does not have the id cached, it creates a token. When Redis comes back up, continue to use the cache.

## Run locally

Start a local redis-server:
```
redis-server
```
Then start the Spring app:
```
gradle bootRun -Predis
```

## Run on CF
Build the project:
```
grable build
```
Push the app by providing the path to the app jar file:
```
cf push <app_name> -p build/libs/redis-cache-failure-handling*.jar
```

Create a redis service and bind it to the app. Spring auto-configures Redis connection using the service-binding so it should just work.
Reference: https://docs.cloudfoundry.org/buildpacks/java/configuring-service-connections/spring-service-bindings.html#redis

## Key points
* `RedisCacheConfig` and `RedisCacheError` intercept the errors when Redis is missing. This allows the request to fall through to the expensive token generation call.
