package io.pivotal.pcfredis.multiredis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenRepository {

    @Autowired
    private TokenSecondaryRepository tokenSecondaryRepository;

    public Token find(String id) {
        return findOnFirstCache(id);
    }

    @Cacheable (cacheManager = "primaryCacheManager", cacheNames = "tokens")
    public Token findOnFirstCache(String id) {
        System.out.println("---> Primary cache miss for id: '" + id + "'");
        return tokenSecondaryRepository.find2(id);
    }

    public static Token verySlowGenerateToken(String id) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Token(id, UUID.randomUUID().toString());
    }
}
