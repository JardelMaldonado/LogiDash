package com.jardel.LogiDash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LogiDashApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogiDashApplication.class, args);
	}

}
