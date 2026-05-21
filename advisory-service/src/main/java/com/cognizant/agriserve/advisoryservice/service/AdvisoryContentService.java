package com.cognizant.agriserve.advisoryservice.service;

import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentResponseDTO;

import java.util.List;

public interface AdvisoryContentService {

    // Updated: Takes uploaderId directly from the Gateway header
    AdvisoryContentResponseDTO saveContent(AdvisoryContentRequestDTO content, Long uploaderId);

    // Returns a list of DTOs for the UI/Frontend
    List<AdvisoryContentResponseDTO> getAllActiveContent();

    // Updated: Takes requesterId and isAdmin boolean for ownership verification
    void softDeleteContent(Long id, Long requesterId, boolean isAdmin);
}