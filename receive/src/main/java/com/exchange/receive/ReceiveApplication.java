package com.exchange.receive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReceiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiveApplication.class, args);
    }

}
