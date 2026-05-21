package com.cognizant.agriserve.advisoryservice.dto;

import lombok.Data;

@Data
public class AdvisoryContentResponseDTO {
    private Long contentId;
    private String title;
    private String category;
    private String fileUri;
    private String description;
    private String status;
}