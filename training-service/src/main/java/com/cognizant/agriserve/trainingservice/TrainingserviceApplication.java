package com.cognizant.agriserve.trainingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TrainingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingserviceApplication.class, args);
	}

}
