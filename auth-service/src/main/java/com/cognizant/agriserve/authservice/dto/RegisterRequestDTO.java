package com.cognizant.agriserve.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    // --- For User Service ---

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Contact info is required")
    @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits")
    private String contactInfo;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(?i)(Farmer)$", message = "Invalid role specified")
    private String role;

    // --- The Crucial Link ---

    // Note: Usually left unvalidated on initial user registration because the
    // database generates it, but if it is passed between services, you can use @Positive
    private Long userId;

    // --- For Farmer Service ---

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dob;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(?i)(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @NotBlank(message = "Address is required")
    @Size(max = 250, message = "Address cannot exceed 250 characters")
    private String address;

    @NotNull(message = "Land size is required")
    @Positive(message = "Land size must be a positive value")
    private Double landSize;

    @NotBlank(message = "Crop type is required")
    @Size(max = 100, message = "Crop type cannot exceed 100 characters")
    private String cropType;
}