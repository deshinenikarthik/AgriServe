package com.cognizant.agriserve.advisoryservice.service.impl;

import com.cognizant.agriserve.advisoryservice.client.FarmerClient;
import com.cognizant.agriserve.advisoryservice.client.UserClient;
import com.cognizant.agriserve.advisoryservice.dao.AdvisoryContentRepository;
import com.cognizant.agriserve.advisoryservice.dao.AdvisorySessionRepository;
import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionResponseDTO;
import com.cognizant.agriserve.advisoryservice.dto.FarmerResponseDTO; // Updated from FarmerDto
import com.cognizant.agriserve.advisoryservice.dto.UserResponseDTO;   // Updated from UserDto
import com.cognizant.agriserve.advisoryservice.entity.AdvisoryContent;
import com.cognizant.agriserve.advisoryservice.entity.AdvisorySession;
import com.cognizant.agriserve.advisoryservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.advisoryservice.service.AdvisorySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisorySessionServiceImpl implements AdvisorySessionService {

    private final AdvisorySessionRepository sessionRepo;
    private final AdvisoryContentRepository contentRepo;
    private final ModelMapper modelMapper;

    private final FarmerClient farmerClient;
    private final UserClient userClient;

    @Override
    public AdvisorySessionResponseDTO logAdvisorySession(AdvisorySessionRequestDTO dto, Long officerId) {

        // Note: The check enforcing that only ExtensionOfficers can log sessions
        // is now securely handled by @PreAuthorize in your Controller!

        // Fetch Farmer Profile via Feign
        FarmerResponseDTO farmer = farmerClient.getFarmerById(dto.getFarmerId());

        // Fetch Advisory Content from local DB
        AdvisoryContent content = contentRepo.findById(dto.getContentId())
                .orElseThrow(() -> new ResourceNotFoundException("Advisory Content not found with ID: " + dto.getContentId()));

        AdvisorySession session = new AdvisorySession();
        session.setFarmerId(farmer.getFarmerId());
        session.setOfficerId(officerId); // Injected directly from the Gateway header!
        session.setContent(content);
        session.setFeedback(dto.getFeedback());
        session.setDate(LocalDateTime.now());
        session.setStatus("Completed");

        AdvisorySession saved = sessionRepo.save(session);
        log.info("Advisory Session ID {} logged successfully by Officer ID {}", saved.getSessionId(), officerId);

        // Map and enrich the response DTO
        AdvisorySessionResponseDTO response = modelMapper.map(saved, AdvisorySessionResponseDTO.class);
        response.setFarmerName(farmer.getName());
        response.setContentTitle(content.getTitle());

        // We do one Feign call here just to get the Officer's name for the response UI
        try {
            UserResponseDTO officer = userClient.findById(officerId);
            response.setOfficerName(officer.getName());
        } catch (Exception e) {
            response.setOfficerName("Unknown Officer");
        }

        return response;
    }

    @Override
    public List<AdvisorySessionResponseDTO> getFarmerHistory(Long farmerId) {
        // NOTE: Make sure your repository uses findByFarmerId(farmerId) instead of the
        // old monolithic findByFarmer_FarmerId(farmerId) if your entity dropped the Farmer relationship!
        return sessionRepo.findByFarmerId(farmerId).stream()
                .map(this::enrichSessionDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdvisorySessionResponseDTO> findAllSessions() {
        return sessionRepo.getAllSessions().stream()
                .map(this::enrichSessionDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUsageAnalytics() {
        return sessionRepo.getContentUsageReport();
    }

    @Override
    public AdvisorySessionResponseDTO getAdvisoryById(Long id) {

        AdvisorySession session = sessionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advisory Session not found with ID: " + id));

        // 2. The Magic One-Liner!
        // This tells ModelMapper to look at 'session' and copy all matching fields into a new 'AdvisorySessionResponseDTO'
        return modelMapper.map(session, AdvisorySessionResponseDTO.class);
    }

    @Override
    public Boolean verifyAdvisorySessionExists(Long sessionId) {
        // Renamed from isExists to match what your ComplianceService is calling via Feign!
        return sessionRepo.existsById(sessionId);
    }


    // ==========================================
    // HELPER METHOD FOR ENRICHING DTOs
    // ==========================================
    private AdvisorySessionResponseDTO enrichSessionDetails(AdvisorySession s) {
        AdvisorySessionResponseDTO res = modelMapper.map(s, AdvisorySessionResponseDTO.class);

        // Try-catches prevent your entire history list from failing if another microservice blinks offline
        try {
            res.setFarmerName(farmerClient.getFarmerById(s.getFarmerId()).getName());
        } catch(Exception e) {
            res.setFarmerName("Unknown Farmer");
        }

        try {
            res.setOfficerName(userClient.findById(s.getOfficerId()).getName());
        } catch(Exception e) {
            res.setOfficerName("Unknown Officer");
        }

        if (s.getContent() != null) {
            res.setContentTitle(s.getContent().getTitle());
        }

        return res;
    }


}