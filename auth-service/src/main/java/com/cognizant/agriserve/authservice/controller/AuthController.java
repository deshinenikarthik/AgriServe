package com.cognizant.agriserve.authservice.controller;

import com.cognizant.agriserve.authservice.dto.LoginRequestDTO;
import com.cognizant.agriserve.authservice.dto.LoginResponseDTO;
import com.cognizant.agriserve.authservice.dto.RegisterRequestDTO;
import com.cognizant.agriserve.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authservice;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authservice.register(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        log.info("Login request received for: {}", loginRequestDTO.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(authservice.login(loginRequestDTO));
    }
}