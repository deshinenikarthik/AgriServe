package com.cognizant.agriserve.advisoryservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdvisorySessionResponseDTO {
    private Long sessionId;
    private String farmerName; // Mapped from Farmer entity
    private String officerName; // Mapped from User entity
    private String contentTitle; // Mapped from AdvisoryContent entity
    private LocalDateTime date;
    private String status;
    private String feedback;
}