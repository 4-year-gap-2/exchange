package com.exchange.order_completed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderCompletedApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderCompletedApplication.class, args);
	}

}
