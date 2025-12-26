package com.theshowsoftware.UpDownProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UpDownProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpDownProjectApplication.class, args);
	}

}
