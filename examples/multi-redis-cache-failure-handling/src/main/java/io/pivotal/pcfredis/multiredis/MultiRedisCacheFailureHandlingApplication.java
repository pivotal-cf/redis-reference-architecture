package io.pivotal.pcfredis.multiredis;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MultiRedisCacheFailureHandlingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(MultiRedisCacheFailureHandlingApplication.class)
                .profiles("app").run(args);
    }
}