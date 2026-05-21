package com.cognizant.agriserve.advisoryservice.client;

import com.cognizant.agriserve.advisoryservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @GetMapping("/api/users/email/{email}")
    UserResponseDTO findByEmail(@PathVariable String email);

    @GetMapping("/api/users/{id}")
    UserResponseDTO findById(@PathVariable Long id);
}
