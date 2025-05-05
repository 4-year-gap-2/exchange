package com.exchange.order_completed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
public class OrderCompletedApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderCompletedApplication.class, args);
	}

}
