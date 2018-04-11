# Redis Reference Architecture

This repo contains reference architecture in the form of Spring apps
to fulfill frequent use cases that can be used with the [Redis for PCF](http://docs.pivotal.io/redis)
offering.

The example applications in this repo are written in Java and make use of the compatibility of
[Spring](https://docs.cloudfoundry.org/buildpacks/java/getting-started-deploying-apps/gsg-spring.html)
with Cloud Foundry bound services.


## redis-cache-failure-handling
This example architecture uses only one Redis server and is intended for lightweight caching.


## multiple-redis-high-availability
This example architecture uses two Redis servers in order to ensure availability of the Redis cache in the event of downtime for one server.
Note that this does not guarantee any additional uptime beyond the SLI/SLO.

### Uptime during an on-demand Redis upgrade
During an upgrade-all errand that is available to on-demand Redis services, all Redis servers could
potentially upgrade all in parallel which would result in all servers having downtime at the same time.
However, if `max-in-flight` is less than the total number of Redis servers (i.e. 1) or `canaries` is less than the total
but greater than 0, then there will always be at least one Redis server cache available to handle requests.
