package com.cognizant.agriserve.advisoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdvisoryContentRequestDTO {
    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Category is mandatory")
    private String category;

    private String fileUri;
    private String description;
}