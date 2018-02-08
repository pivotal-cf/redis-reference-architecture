# Cache Failure Handling Example

Sample app which takes request at `/token?id=<something>` and returns a token.
When Redis is missing or does not have the id cached, it creates a token. When Redis comes back up, continue to use the cache.

## Run locally

Start a local redis-server:
```
redis-server
```
Then start the Spring app:
```$xslt
gradle bootRun -Predis
```

## Run on CF
TODO

## Key points
* `RedisCacheConfig` and `RedisCacheError` intercept the errors when Redis is missing. This allows the request to fall through to the expensive token generation call.
