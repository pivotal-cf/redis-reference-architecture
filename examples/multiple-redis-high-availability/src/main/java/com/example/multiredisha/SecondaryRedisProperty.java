package com.example.multiredisha;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Qualifier("secondaryRedisProperty")
@ConfigurationProperties(prefix = "spring.redis.secondary")
public class SecondaryRedisProperty extends RedisProperty {

    @PostConstruct
    public Void PrintPort(){
        System.out.println("SECONDARY PORT: "+ this.getPort());
        return null;
    }
}
