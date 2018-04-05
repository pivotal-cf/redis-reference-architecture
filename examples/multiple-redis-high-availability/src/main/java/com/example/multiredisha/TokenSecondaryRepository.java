package com.example.multiredisha;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TokenSecondaryRepository {

    @Cacheable(cacheManager = "secondaryCacheManager", cacheNames = "tokens")
    public Token find2(String id){
        System.out.println("---> Secondary cache miss for id: '" + id + "'");
        return TokenRepository.verySlowGenerateToken(id);
    }
}
