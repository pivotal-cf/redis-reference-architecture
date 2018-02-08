package com.example.rediscachefailurehandling;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@CacheConfig(cacheNames = "tokens")
public class TokenRepository {
    @Cacheable
    public Token find(String id) {
        System.out.println("---> Cache miss for id: '" + id + "'");
        return verySlowGenerateToken(id);
    }

    private static Token verySlowGenerateToken(String id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Token(id, UUID.randomUUID().toString());
    }
}
