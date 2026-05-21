package com.cognizant.agriserve.advisoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorySessionRequestDTO {

    @NotNull(message = "Farmer ID cannot be null")
    @Min(value = 1, message = "Farmer ID must be a positive number")
    private Long farmerId;

    @NotNull(message = "Content ID cannot be null")
    @Min(value = 1, message = "Content ID must be a positive number")
    private Long contentId;

    @NotBlank(message = "Feedback/Notes cannot be blank")
    @Size(min = 10, max = 500, message = "Feedback must be between 10 and 500 characters")
    private String feedback;
}