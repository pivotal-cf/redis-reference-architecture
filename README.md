# Redis Reference Architectures

The purpose of this repository is to make it easier to use the Pivotal Cloud Foundry Redis Tile [Redis for PCF](http://docs.pivotal.io/redis). The repository contains example Cloud Foundry Apps that demonstrate using Redis for PCF.

These example applications are written in Java and make use of the compatibility of
[Spring](https://docs.cloudfoundry.org/buildpacks/java/getting-started-deploying-apps/gsg-spring.html) with Cloud Foundry bound services.

## Pros and Cons of the Example App
Redis can be used for storing a wide range of data. No reference architecture is a good fit with all possible uses of Redis. Our existing examples focus on using redis as a cache. If you require that the data in Redis be stored in a specific structure, such as a specific list or set, this is likely not possible using Spring Cache. We do not yet have a reference architecture for you.

## Example App:
[__Redis Cache Failure Handling__](https://github.com/pivotal-cf/redis-reference-architecture/tree/master/examples/redis-cache-failure-handling)
  > This example architecture uses the [Spring Cache framework](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html) with a single Redis service to cache calls to a java method.
  * Appropriate if you have a method that will benefit from caching:
    - the method is slow to run
    - the method always returns the same result for a given input (similar to a pure function, but may have idempotent side effects)
    - the method is called repeatedly with the same input
  * Appropriate if your App can handle a short period of time when the cache is unavailable
    - Redis for PCF service instances become unavailable for a few minutes during upgrades
  * Not appropriate if you require direct access and manipulation of the underlying Redis cache
