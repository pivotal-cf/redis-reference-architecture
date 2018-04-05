package com.example.multiredisha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Autowired
    @Qualifier("primaryRedisProperty")
    private PrimaryRedisProperty primaryRedisProperty;

    @Autowired
    @Qualifier("secondaryRedisProperty")
    private SecondaryRedisProperty secondaryRedisProperty;

    public RedisConnectionFactory primaryRedisConnectionFactory() {
        return getRedisConnectionFactory(primaryRedisProperty.getHost(), primaryRedisProperty.getPort(), primaryRedisProperty.getDatabase());
    }


    @Qualifier("primaryRedisTemplate")
    @Bean(name = "primaryRedisTemplate")
    public RedisTemplate primaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(primaryRedisConnectionFactory());
        return redisTemplate;
    }

    public RedisConnectionFactory secondaryRedisConnectionFactory() {
        return getRedisConnectionFactory(secondaryRedisProperty.getHost(), secondaryRedisProperty.getPort(), secondaryRedisProperty.getDatabase());
    }


    @Qualifier("secondaryRedisTemplate")
    @Bean(name = "secondaryRedisTemplate")
    public RedisTemplate secondaryRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(secondaryRedisConnectionFactory());
        return redisTemplate;
    }

    private RedisConnectionFactory getRedisConnectionFactory(String host, int port, int database) {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(host);
        standaloneConfiguration.setPort(port);
        standaloneConfiguration.setDatabase(database);

        return new JedisConnectionFactory(standaloneConfiguration);
    }
}
