package com.cognizant.agriserve.advisoryservice.service;

import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionResponseDTO;

import java.util.List;
import java.util.Map;

public interface AdvisorySessionService {

    // Updated: Takes officerId directly from the Gateway header instead of the Security Context
    AdvisorySessionResponseDTO logAdvisorySession(AdvisorySessionRequestDTO dto, Long officerId);

    // Returns a history list of ResponseDTOs for a specific farmer
    List<AdvisorySessionResponseDTO> getFarmerHistory(Long farmerId);

    // Returns a list of all sessions across the platform
    List<AdvisorySessionResponseDTO> findAllSessions();

    // Returns a map for the Manager's dashboard analytics
    List<Map<String, Object>> getUsageAnalytics();

    // Updated: Renamed to perfectly match the Feign Client call from the Compliance Service
    Boolean verifyAdvisorySessionExists(Long sessionId);

    AdvisorySessionResponseDTO getAdvisoryById(Long id);
}