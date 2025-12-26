package com.theshowsoftware.InternalTestPage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InternalTestPageApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternalTestPageApplication.class, args);
	}

}
