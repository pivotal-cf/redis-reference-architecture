package io.pivotal.pcfredis.multiredis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Qualifier("primaryRedisProperty")
@ConfigurationProperties(prefix = "spring.redis.primary")
public class PrimaryRedisProperty extends RedisProperty {

    @PostConstruct
    public Void PrintPort(){
        System.out.println("PRIMARY PORT: "+ this.getPort());
        return null;
    }
}
