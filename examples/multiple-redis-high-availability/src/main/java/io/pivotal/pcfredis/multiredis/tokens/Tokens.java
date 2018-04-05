package io.pivotal.pcfredis.multiredis.tokens;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import static java.util.UUID.randomUUID;

@Component
class TokenRepositoryPrimaryCache {

    private final TokenRepositorySecondaryCache tokenRepositorySecondaryCache;

    @Autowired
    public TokenRepositoryPrimaryCache(TokenRepositorySecondaryCache tokenRepositorySecondaryCache) {
        this.tokenRepositorySecondaryCache = tokenRepositorySecondaryCache;
    }

    @Cacheable(cacheManager = "primaryCacheManager", cacheNames = "tokens")
    public Token find(String id) {
        System.out.println("---> Primary cache miss for id: '" + id + "'");
        return tokenRepositorySecondaryCache.find(id);
    }
}



@Component
class TokenRepositorySecondaryCache {

    @Cacheable(cacheManager = "secondaryCacheManager", cacheNames = "tokens")
    public Token find(String id){
        System.out.println("---> Secondary cache miss for id: '" + id + "'");
        return verySlowGenerateToken(id);
    }

    private static Token verySlowGenerateToken(String id) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Token(id, randomUUID().toString());
    }
}


@Repository
public class Tokens {
    private final TokenRepositoryPrimaryCache tokenRepository;

    @Autowired
    public Tokens(TokenRepositoryPrimaryCache tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token find(String id) {
        return this.tokenRepository.find(id);
    }

}
