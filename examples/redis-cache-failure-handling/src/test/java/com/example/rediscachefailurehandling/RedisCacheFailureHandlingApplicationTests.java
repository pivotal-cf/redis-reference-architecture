package com.example.rediscachefailurehandling;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisCacheFailureHandlingApplicationTests {

	@Test
	public void contextLoads() {
	}


	@Autowired
	private TestRestTemplate restTemplate;


	@Test
	public void should_create_new_token_when_it_does_not_exist_on_cache() {
		ResponseEntity<Token> myToken = this.restTemplate.getForEntity("/token?id={t}", Token.class, "someId");

		assertThat(myToken.getBody(), notNullValue());
	}
}
