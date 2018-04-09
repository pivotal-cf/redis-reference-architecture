package io.pivotal.pcfredis.multiredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


@Configuration
@Profile("default")
class MultipleRedisHighAvailabilityDefaultConfig {

    private final PrimaryRedisProperty primaryRedisProperty;
    private final SecondaryRedisProperty secondaryRedisProperty;

    @Autowired
    public MultipleRedisHighAvailabilityDefaultConfig(PrimaryRedisProperty primaryRedisProperty, SecondaryRedisProperty secondaryRedisProperty) {
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
        standaloneConfiguration.setPassword(redisProperty.getPassword());

        return new JedisConnectionFactory(standaloneConfiguration);
    }
}


@Configuration
@Profile("cloud")
class MultipleRedisHighAvailabilityCloudConfig {

    private List<RedisServiceInfo> redisInfos;

    @Autowired
    public MultipleRedisHighAvailabilityCloudConfig() {
        this.redisInfos = this.getRedisServiceInfos();
    }

    private List<RedisServiceInfo> getRedisServiceInfos() {
        CloudFactory cloudFactory = new CloudFactory();
        Cloud cloud = cloudFactory.getCloud();
        List<RedisServiceInfo> redisServiceInfos = cloud.getServiceInfosByType(RedisServiceInfo.class);

        assert redisServiceInfos.size() == 2 : "There should be 2 redis service instances bound to this App. " + redisServiceInfos.size() + " found.";
        return redisServiceInfos;
    }


    @Qualifier("primaryRedisTemplate")
    @Bean(name = "primaryRedisTemplate")
    public RedisTemplate primaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisServiceInfo primaryInfo = redisInfos.get(0);
        RedisConnectionFactory primaryRedisConnectionFactory = getRedisConnectionFactory(primaryInfo);

        redisTemplate.setConnectionFactory(primaryRedisConnectionFactory);
        return redisTemplate;
    }

    @Qualifier("secondaryRedisTemplate")
    @Bean(name = "secondaryRedisTemplate")
    public RedisTemplate secondaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        RedisServiceInfo secondaryInfo = redisInfos.get(1);
        RedisConnectionFactory secondaryRedisConnectionFactory = getRedisConnectionFactory(secondaryInfo);

        redisTemplate.setConnectionFactory(secondaryRedisConnectionFactory);
        return redisTemplate;
    }

    private RedisConnectionFactory getRedisConnectionFactory(RedisServiceInfo redisInfo) {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(redisInfo.getHost());
        standaloneConfiguration.setPort(redisInfo.getPort());
        standaloneConfiguration.setPassword(RedisPassword.of(redisInfo.getPassword()));

        return new JedisConnectionFactory(standaloneConfiguration);
    }

}

@Component
@Profile("default")
@ConfigurationProperties(prefix = "spring.redis.secondary")
class SecondaryRedisProperty extends RedisStandaloneConfiguration {
}

@Component
@Profile("default")
@ConfigurationProperties(prefix = "spring.redis.primary")
class PrimaryRedisProperty extends RedisStandaloneConfiguration {
}

@Configuration
class RedisCacheConfig implements CachingConfigurer {

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

    @Override
    public CacheManager cacheManager() {
        return null;
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return null;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheError();
    }
}

class RedisCacheError implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception,
                                    Cache cache, Object key) {
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache,
                                    Object key, Object value) {
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache,
                                      Object key) {
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
    }
}
