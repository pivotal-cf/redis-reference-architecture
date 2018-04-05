package io.pivotal.pcfredis.multiredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Configuration
public class MultipleRedisHighAvailabilityConfig {
    private final PrimaryRedisProperty primaryRedisProperty;

    private final SecondaryRedisProperty secondaryRedisProperty;

    @Autowired
    public MultipleRedisHighAvailabilityConfig(PrimaryRedisProperty primaryRedisProperty, SecondaryRedisProperty secondaryRedisProperty) {
        this.primaryRedisProperty = primaryRedisProperty;
        this.secondaryRedisProperty = secondaryRedisProperty;
    }

    @Qualifier("primaryRedisTemplate")
    @Bean(name = "primaryRedisTemplate")
    public RedisTemplate primaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisConnectionFactory primaryRedisConnectionFactory = getRedisConnectionFactory(primaryRedisProperty);
        redisTemplate.setConnectionFactory(primaryRedisConnectionFactory);
        return redisTemplate;
    }

    @Qualifier("secondaryRedisTemplate")
    @Bean(name = "secondaryRedisTemplate")
    public RedisTemplate secondaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisConnectionFactory secondaryRedisConnectionFactory = getRedisConnectionFactory(secondaryRedisProperty);
        redisTemplate.setConnectionFactory(secondaryRedisConnectionFactory);
        return redisTemplate;
    }

    private RedisConnectionFactory getRedisConnectionFactory(RedisStandaloneConfiguration redisProperty) {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(redisProperty.getHostName());
        standaloneConfiguration.setPort(redisProperty.getPort());
        standaloneConfiguration.setDatabase(redisProperty.getDatabase());

        return new JedisConnectionFactory(standaloneConfiguration);
    }
}

@Component
@ConfigurationProperties(prefix = "spring.redis.secondary")
class SecondaryRedisProperty extends RedisStandaloneConfiguration {}

@Component
@ConfigurationProperties(prefix = "spring.redis.primary")
class PrimaryRedisProperty extends RedisStandaloneConfiguration {}

@Configuration
class RedisCacheConfig {

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