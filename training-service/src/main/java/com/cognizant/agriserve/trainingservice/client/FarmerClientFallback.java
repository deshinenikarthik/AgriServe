package com.cognizant.agriserve.trainingservice.client;

import com.cognizant.agriserve.trainingservice.dto.response.FarmerResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FarmerClientFallback implements FarmerClient {

    @Override
    public FarmerResponseDTO getFarmerByUserId(Long userId) {
        // Throw a custom exception that your GlobalExceptionHandler can catch
        throw new RuntimeException("Farmer Service is currently unreachable. Workshop registration paused.");

    }
}