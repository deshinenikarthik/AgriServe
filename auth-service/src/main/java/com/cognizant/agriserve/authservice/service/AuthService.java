package com.cognizant.agriserve.authservice.service;

import com.cognizant.agriserve.authservice.dto.LoginRequestDTO;
import com.cognizant.agriserve.authservice.dto.LoginResponseDTO;
import com.cognizant.agriserve.authservice.dto.RegisterRequestDTO;
import com.cognizant.agriserve.authservice.dto.UserDTO;
import com.cognizant.agriserve.authservice.client.FarmerClient;
import com.cognizant.agriserve.authservice.client.UserClient;
import com.cognizant.agriserve.authservice.security.CustomUserDetails;
import com.cognizant.agriserve.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    private final UserClient userClient;
    private final FarmerClient farmerClient;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;


    public String register(RegisterRequestDTO dto) {
        log.info("Attempting to register new Farmer with email: {}", dto.getEmail());

        // 1. Force the role
        dto.setRole("Farmer");

        // 2. Save to User Service & Capture the Response
        UserDTO savedUser = userClient.register(dto);

        if (savedUser == null || savedUser.getUserId() == null) {
            log.error("Registration failed: User Service returned a null ID. Check UserDTO mapping.");
            throw new RuntimeException("User registration failed: User ID was not generated.");
        }

        log.info("User created successfully with ID: {}. Linking Farmer profile...", savedUser.getUserId());

        // Explicitly set the userId into the DTO for the Farmer Service call
        dto.setUserId(savedUser.getUserId());

        // 3. Save to Farmer Service with Compensating Transaction (Rollback)
        try {
            farmerClient.registerFarmer(dto);
            log.info("Successfully registered farmer profile in FARMER-SERVICE for User ID: {}", savedUser.getUserId());

            return "Farmer registered successfully with ID: " + savedUser.getUserId();

        } catch (Exception e) {
            log.error("Farmer Profile Creation Failed for User ID: {}. Initiating rollback...", savedUser.getUserId());

            // COMPENSATING TRANSACTION: Delete the user that was just created
            try {
                // Note: Make sure 'deleteUser' exists in your userClient interface!
                userClient.deleteUser(savedUser.getUserId());
                log.info("Rollback successful: Deleted orphaned user ID: {}", savedUser.getUserId());
            } catch (Exception rollbackEx) {
                // If the rollback fails, we have a critical data inconsistency requiring manual intervention
                log.error("CRITICAL DATA INCONSISTENCY: Rollback failed for User ID: {}. An orphaned record exists in the User database! Rollback Error: {}",
                        savedUser.getUserId(), rollbackEx.getMessage());
            }

            // Alert the frontend/user that the process failed
            throw new RuntimeException("Registration failed during Farmer profile setup. Account creation was rolled back. Reason: " + e.getMessage());
        }
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        log.info("User {} authenticated successfully", dto.getEmail());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            throw new RuntimeException("No roles assigned to the user");
        }

        String role = roles.get(0);
        Long userId = userDetails.getUserId();

        String token = jwtUtil.generateToken(userDetails.getUsername(), role, userId);

        // 👇 UPDATED RETURN STATEMENT
        return new LoginResponseDTO(
                token,
                userId,
                role,
                userDetails.getName(),        // Fetched from CustomUserDetails
                userDetails.getUsername(),    // getUsername() returns the email
                userDetails.getContactInfo()  // Fetched from CustomUserDetails
        );
    }
}