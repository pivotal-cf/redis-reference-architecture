package io.pivotal.pcfredis.multiredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisCacheConfig {

    private final RedisTemplate primaryRedisTemplate;

    private final RedisTemplate secondaryRedisTemplate;

    @Autowired
    public RedisCacheConfig(
            @Qualifier("primaryRedisTemplate") RedisTemplate primaryRedisTemplate,
            @Qualifier("secondaryRedisTemplate") RedisTemplate secondaryRedisTemplate) {
        this.primaryRedisTemplate = primaryRedisTemplate;
        this.secondaryRedisTemplate = secondaryRedisTemplate;
    }

    @Primary
    @Bean(name = "primaryCacheManager")
    public CacheManager primaryCacheManager() {
        System.out.println("Creating primary cache manager");
        return RedisCacheManager
                .builder(primaryRedisTemplate.getConnectionFactory())
                .build();
    }

    @Bean(name = "secondaryCacheManager")
    public CacheManager secondaryCacheManager() {
        System.out.println("Creating secondary cache manager");
        return RedisCacheManager
                .builder(secondaryRedisTemplate.getConnectionFactory())
                .build();
    }
}