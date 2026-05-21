package com.cognizant.agriserve.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponseDTO {
    String token;
    private Long userId;
    private String role;
    private String name;
    private String email;
    private String contactInfo;
}
