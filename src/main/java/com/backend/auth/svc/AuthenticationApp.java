package com.backend.auth.svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthenticationApp {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationApp.class, args);
	}

}
