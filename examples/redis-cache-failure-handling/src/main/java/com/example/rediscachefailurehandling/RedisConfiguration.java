package com.example.rediscachefailurehandling;

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class RedisConfiguration {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        CfService service = getService();

        if (service != null) {
            logger.info("service instance {} found, checking for sentinel creds...", service.getName());
            Object sentinels = service.getCredentials().getMap().getOrDefault("sentinels", null);

            if (!Objects.isNull(sentinels)) {
                logger.info("found service {} with sentinel creds, creating redis client with sentinel configs", service.getName());
                return redisCacheManagerWithSentinelConfigs(service, redisConnectionFactory);
            }
        }

        logger.info("no service instance found with sentinel creds, continuing with defaults");

        return RedisCacheManager.create(redisConnectionFactory);
    }

    private RedisCacheManager redisCacheManagerWithSentinelConfigs(CfService service, RedisConnectionFactory redisConnectionFactory) {
        List<Map<String, Object>> sentinels;

        try {
            sentinels = (List<Map<String, Object>>) service.getCredentials().getMap().get("sentinels");
        } catch (Exception e) {
            logger.error("error destructuring credentials to java types, err: {}", e.toString());
            return RedisCacheManager.create(redisConnectionFactory);
        }

        Set<String> hostPorts = sentinels.stream()
                .map(sentinel -> String.format("%s:%d", sentinel.getOrDefault("host", ""), (Integer) sentinel.getOrDefault("port", 0)))
                .collect(Collectors.toSet());

        RedisCacheConfiguration redisConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues();

        String masterName = service.getCredentials().getString("master_name");
        String redisPassword = service.getCredentials().getPassword();

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(masterName, hostPorts);
        redisSentinelConfiguration.setPassword(redisPassword);

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisSentinelConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();

        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(redisConfig)
                .build();
    }

    private CfService getService() {
        CfEnv cfEnv = new CfEnv();
        CfService service = null;

        try {
            service = cfEnv.findServiceByTag("redis", "pivotal");
        } catch (Exception e) {
            logger.error("failed to find service with tag 'redis' and 'pivotal', error: {}", e.toString());
        }

        if (Objects.isNull(System.getenv("service_name")) || System.getenv("service_name").isEmpty()) {
            return service;
        }

        String serviceName = System.getenv("service_name");
        try {
            service = cfEnv.findServiceByName(serviceName);
        } catch (Exception e) {
            logger.error("failed to find service by name {}, error: {}", serviceName, e.toString());
        }

        if (service != null) {
            logger.info("successfully found service with name {}", service.getName());
            return service;
        }

        return service;
    }

}
