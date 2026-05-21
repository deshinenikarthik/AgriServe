package com.cognizant.agriserve.advisoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AdvisoryServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdvisoryServiceApplication.class, args);
	}
}
