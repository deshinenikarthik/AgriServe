package com.cognizant.agriserve.trainingservice.dto.response;

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

    private Long farmerId;

    private String name;

    private String dob;

    private String gender;

    private String address;

    private String contactInfo;

    private Double landSize;

    private String cropType;

    private String status;

    private Long userId;

}