package com.cognizant.agriserve.trainingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramResponseDTO {

    private Long programId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long managerId;

    // In a microservice, you might eventually add a field here like:
    // private String managerName;
    // (Fetched from the User Service before sending to the frontend!)
}
