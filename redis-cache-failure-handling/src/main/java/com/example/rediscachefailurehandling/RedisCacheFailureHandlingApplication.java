package com.example.rediscachefailurehandling;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RedisCacheFailureHandlingApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(RedisCacheFailureHandlingApplication.class)
				.profiles("app").run(args);
	}
}
