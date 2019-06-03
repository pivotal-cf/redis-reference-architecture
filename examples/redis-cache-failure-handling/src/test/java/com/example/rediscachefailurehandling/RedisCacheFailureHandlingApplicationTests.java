package com.example.rediscachefailurehandling;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisCacheFailureHandlingApplicationTests {

	private String tokenId = "123";
	private Token token = new Token(tokenId, "token-data", 0);

	@Autowired
	private TestRestTemplate restTemplate;

	@SpyBean
	private CacheManager manager;

	@Test
	public void shouldCreateNewTokenWhenItDoesNotExistOnCache() {
		mockCache(manager, null);
		Token finalToken = retrieveToken(tokenId);

		assertThat(finalToken, notNullValue());
	}

	@Test
	public void shouldGetTokenFromCache() {
		mockCache(manager, new TokenValueWrapper());
		Token finalToken = retrieveToken(tokenId);
		assertThat(finalToken, samePropertyValuesAs(token));
	}

	private Token retrieveToken(String tokenId) {
		return this.restTemplate.getForObject("/token?id={t}", Token.class, tokenId);
	}

	private Cache mockCache(CacheManager manager, TokenValueWrapper tokenVW) {
		Cache cache = mock(Cache.class);
		when(cache.get(any())).thenReturn(tokenVW);
		doReturn(cache).when(manager).getCache("tokens");
		return cache;
	}

	class TokenValueWrapper implements Cache.ValueWrapper {

		@Override
		public Object get() {
			return token;
		}
	}
}
