package io.pivotal.pcfredis.multiredis;

import io.pivotal.pcfredis.multiredis.tokens.Token;
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
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultipleRedisHighAvailabilityApplicationIT {

    private String tokenID = "123";
    private Token token = new Token(tokenID, "token-data");

    @Autowired
    private TestRestTemplate restTemplate;

    @Qualifier("primaryCacheManager")
    @SpyBean
    private CacheManager primaryCacheManager;

    @Qualifier("secondaryCacheManager")
    @SpyBean
    private CacheManager secondaryCacheManager;


    @Test
    public void
    should_look_in_the_first_redis_for_tokens() {
        mockCache(primaryCacheManager, new tokenValueWrapper());

        Token finalToken = retrieveToken(tokenID);
        assertThat(token, is(finalToken));
    }

    @Test
    public void
    should_look_in_the_second_redis_when_the_first_does_not_have_it() {
        mockCache(primaryCacheManager, null);
        mockCache(secondaryCacheManager, new tokenValueWrapper());

        Token finalToken = retrieveToken(tokenID);

        assertThat(token, is(finalToken));
    }

    @Test
    public void
    should_create_a_token_when_token_is_not_in_either_cache() {
        String tokenId = "new-" + randomUUID().toString();
        mockCache(primaryCacheManager, null);
        mockCache(secondaryCacheManager, null);
        ResponseEntity<Token> myToken = this.restTemplate.getForEntity("/token?id={t}", Token.class, tokenId);

        assertThat(myToken.getBody().getId(), is(tokenId));
    }

    private Token retrieveToken(String tokenId) {
        ResponseEntity<Token> response = this.restTemplate.getForEntity("/token?id={t}", Token.class, tokenId);
        return response.getBody();
    }

    private Cache mockCache(CacheManager manager, tokenValueWrapper tokenVW) {
        Cache cache = mock(Cache.class);
        when(cache.get(any())).thenReturn(tokenVW);
        doReturn(cache).when(manager).getCache("tokens");
        return cache;
    }

    class tokenValueWrapper implements Cache.ValueWrapper {

        @Override
        public Object get() {
            return token;
        }
    }


}
