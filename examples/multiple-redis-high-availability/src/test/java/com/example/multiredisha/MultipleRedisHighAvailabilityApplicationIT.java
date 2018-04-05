package com.example.multiredisha;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultipleRedisHighAvailabilityApplicationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Qualifier("primaryCacheManager")
    @SpyBean
    private CacheManager primaryCacheManager;

    @Qualifier("secondaryCacheManager")
    @SpyBean
    private CacheManager secondaryCacheManager;

    @Test public void
    should_create_a_token_in_the_redis_when_it_does_not_exist() {
        String tokenId = "new-" + randomUUID().toString();
        ResponseEntity<Token> myToken = this.restTemplate.getForEntity("/token?id={t}", Token.class, tokenId);

        assertThat(myToken.getBody().getId(), is(tokenId));
    }

    @Test public void
    should_look_in_the_second_redis_when_the_first_is_down() {
        String tokenId = "when-1st-redis-is-down-" + randomUUID();
        Token initialToken = saveTokenInBothCaches(tokenId);
        turnOffFirstCache();

        Token finalToken = retrieveToken(tokenId);

        assertThat(initialToken, is(finalToken));
    }

    private Token retrieveToken(String tokenId) {
        ResponseEntity<Token> response = this.restTemplate.getForEntity("/token?id={t}", Token.class, tokenId);
        return response.getBody();
    }

    private void turnOffFirstCache() {
        Cache cache = mock(Cache.class);
        when(cache.get(any())).thenReturn(null);
        doReturn(cache).when(primaryCacheManager).getCache("tokens");
    }

    private Token saveTokenInBothCaches(String tokenId) {
        Token token = this.restTemplate.getForEntity("/token?id={t}", Token.class, tokenId).getBody();
        checkExistsInCache(token, primaryCacheManager);
        checkExistsInCache(token, secondaryCacheManager);
        return token;
    }

    private static void checkExistsInCache(Token token, CacheManager cacheManager) {
        Cache.ValueWrapper tokens = cacheManager.getCache("tokens").get(token.getId());
        assertThat(tokens.get(), is(token));
    }


}
