package com.cognizant.agriserve.authservice.client;

import com.cognizant.agriserve.authservice.config.FeignClientInterceptor;
import com.cognizant.agriserve.authservice.dto.RegisterRequestDTO;
import com.cognizant.agriserve.authservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="USER-SERVICE", configuration = FeignClientInterceptor.class)
public interface UserClient {

    @PostMapping("/api/users/register")
    UserDTO register(@RequestBody RegisterRequestDTO registerRequestDTO);

    @PostMapping("/api/users/byusername/{username}")
    UserDTO login(@PathVariable("username") String username);

    @DeleteMapping("/api/users/{userId}")
    void deleteUser(Long userId);
}