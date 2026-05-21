package com.cognizant.agriserve.trainingservice.client;

import com.cognizant.agriserve.trainingservice.dto.response.FarmerResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FARMER-SERVICE", fallback = FarmerClientFallback.class)
public interface FarmerClient {

    @GetMapping("/api/farmers/user/{userId}")
    FarmerResponseDTO getFarmerByUserId(@PathVariable("userId") Long userId);
}