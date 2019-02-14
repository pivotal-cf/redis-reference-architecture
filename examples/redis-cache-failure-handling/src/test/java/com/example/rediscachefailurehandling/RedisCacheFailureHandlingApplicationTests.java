package com.example.rediscachefailurehandling;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisCacheFailureHandlingApplicationTests {

	private String tokenId = "123";
	private Token token = new Token(tokenId, "token-data");

	@Autowired
	private TestRestTemplate restTemplate;

	@SpyBean
	private CacheManager manager;

	@Test
	public void should_create_new_token_when_it_does_not_exist_on_cache() {
		mockCache(manager, null);
		Token finalToken = retrieveToken(tokenId);

		assertThat(finalToken, notNullValue());
	}

	@Test
	public void should_get_token_from_cache() {
		mockCache(manager, new tokenValueWrapper());
		Token finalToken = retrieveToken(tokenId);
		assertThat(finalToken, samePropertyValuesAs(token));
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
