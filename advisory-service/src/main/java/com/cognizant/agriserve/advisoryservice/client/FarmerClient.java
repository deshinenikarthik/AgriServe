package com.cognizant.agriserve.advisoryservice.client;

import com.cognizant.agriserve.advisoryservice.dto.FarmerResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FARMER-SERVICE")
public interface FarmerClient {
    @GetMapping("api/farmers/{farmerId}")
    FarmerResponseDTO getFarmerById(@PathVariable Long farmerId);
}
