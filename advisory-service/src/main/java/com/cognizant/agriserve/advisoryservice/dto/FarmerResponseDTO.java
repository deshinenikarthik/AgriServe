package com.cognizant.agriserve.advisoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FarmerResponseDTO {

    public enum Status {
        PENDING,
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    public enum Gender{
        MALE,
        FEMALE,
        OTHER
    }

    private Long farmerId;
    private String name;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String contactInfo;
    private Double landSize;
    private String cropType;
    private Status status;

    // The underlying Auth User ID attached to this farmer
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}