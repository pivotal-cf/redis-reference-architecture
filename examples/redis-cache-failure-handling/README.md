# Cache Failure Handling Example

This sample Spring App demonstrates using Redis as a cache for a time-intensive method. This could include lookup in another data service, or another slow process such as page generation.
* Appropriate if you have a method that will benefit from caching:
  - the method is slow to run
  - the method always returns the same result for a given input (similar to a pure function, but may have idempotent side effects)
  - the method is called repeatedly with the same input
* Appropriate if your App can handle a short period of time when the cache is unavailable
  - Redis for PCF service instances become unavailable for a few minutes during upgrades
* Not appropriate if you require direct access and manipulation of the underlying Redis cache

---

The sample app takes requests at `/token?id=<something>` and returns a token.
When Redis is missing or does not have the id cached, it creates a token. When Redis comes back up, continue to use the cache.

![Process Diagram](/assets/process_diagram.svg "Process Diagram")

## Run locally

Start a local redis-server:
```
redis-server
```
Then start the Spring app:
```
cd redis-cache-failure-handling
gradle bootRun
```

Visit in your browser:
```
http://localhost:8080/token?id=<ANY_ID>
```
Observe that the `duration` field of the response is long the first time each token is passed. This decreases drastically with subsequent calls when the Redis cache is used.


## Run on CF
Build the project:
```
gradle build
```
Push the app by providing the path to the app jar file:
```
cf push --no-start
```
This will print the route to the App, usually 'apps.<CF_URL>.com'.


Create a redis service. Spring auto-configures Redis connection using the service-binding so it should just work.
```
#cf create-service <REDIS_SERVICE> <SERVICE_PLAN> <SERVICE_INSTANCE_NAME>
 cf create-service  p.redis         cache-small   <SERVICE_INSTANCE_NAME>
```

Bind your redis service, and start the App:
```
cf bind-service redis-cache-example <SERVICE_INSTANCE_NAME>
cf start redis-cache-example
```


## How this works
This example app is using [java-cfenv](https://github.com/pivotal-cf/java-cfenv) to discover service instance credentials.

In order for this to work we need to make some slight modification to the `manifest.yml`, as described in https://spring.io/blog/2019/02/15/introducing-java-cfenv-a-new-library-for-accessing-cloud-foundry-services

## Usage

Once the application is deployed, you can call the the `/token` endpoint in order to retrieve a given `id`. The first time an `id` is used, given it is not present in the cache, a new entry will be created. This operation takes time.

```bash
$ curl http://redis-cache-example.<CF APPS DOMAIN>/token\?id\=new_key
{"data":"b8506113-689f-4fa8-856e-34fb1a1077e1","id":"new_key","duration":3006}
```

After the key is created, it is just a matter of retrieving it in the cache. This operation takes less time compared to when it was created, as we can see in the `duration` property in the response.

```bash
$ curl http://redis-cache-example.<CF APPS DOMAIN>/token\?id\=new_key
{"data":"b8506113-689f-4fa8-856e-34fb1a1077e1","id":"new_key","duration":3}
```

You can figure out that the `CF APPS DOMAIN` is by running `cf domains` or `cf app redis-cache-example`.
