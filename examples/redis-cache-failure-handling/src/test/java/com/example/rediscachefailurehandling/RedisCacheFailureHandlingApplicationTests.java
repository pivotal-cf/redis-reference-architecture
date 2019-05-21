package com.example.rediscachefailurehandling;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

	@Mock
	public Cache cache;

	@Before
	public void setup() {
		doReturn(cache).when(manager).getCache("tokens");
	}

	@Test
	public void shouldCreateNewTokenWhenItDoesNotExistInCache() {
		when(cache.get(tokenId)).thenReturn(() -> null);

		Token finalToken = retrieveToken(tokenId);

		assertThat(finalToken, notNullValue());
		verify(cache).get(tokenId);
	}

	@Test
	public void shouldGetTokenFromCache() {
		when(cache.get(tokenId)).thenReturn(() -> token);

		Token finalToken = retrieveToken(tokenId);

		assertThat(finalToken, samePropertyValuesAs(token));
		verify(cache).get(tokenId);
	}

	private Token retrieveToken(String tokenId) {
		return this.restTemplate.getForObject("/token?id={t}", Token.class, tokenId);
	}
}