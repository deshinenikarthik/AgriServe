package com.cognizant.agriserve.advisoryservice.controller;

import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisorySessionResponseDTO;
import com.cognizant.agriserve.advisoryservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.advisoryservice.service.AdvisorySessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advisory-sessions")
@RequiredArgsConstructor
@Validated
public class AdvisorySessionController {

    private final AdvisorySessionService sessionService;

    @PostMapping("/log")
    @PreAuthorize("hasRole('ExtensionOfficer')")
    public ResponseEntity<AdvisorySessionResponseDTO> logSession(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @Valid @RequestBody AdvisorySessionRequestDTO dto) {

        // Notice how clean this is without the Authentication object!
        return ResponseEntity.ok(sessionService.logAdvisorySession(dto, officerId));
    }

    @GetMapping("/{sessionId}/exists")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager', 'ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<Void> verifyAdvisorySessionExists(@PathVariable Long sessionId){

        // This logic perfectly mimics the Training Service, giving your
        // Compliance Service Feign Client exactly what it expects!
        boolean exists = sessionService.verifyAdvisorySessionExists(sessionId);

        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            throw new ResourceNotFoundException("Advisory Session not found with ID: " + sessionId);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager', 'ComplianceOfficer', 'ExtensionOfficer')")
    public ResponseEntity<List<AdvisorySessionResponseDTO>> getAllSessions() {
        return ResponseEntity.ok(sessionService.findAllSessions());
    }

    @GetMapping("/history/{farmerId}")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager', 'ExtensionOfficer', 'Farmer')")
    public ResponseEntity<List<AdvisorySessionResponseDTO>> getHistory(
            @PathVariable @Min(1) Long farmerId) {

        // Note: Similar to the ParticipationController, if you want to strictly prevent
        // one Farmer from viewing another Farmer's history, you should pass down
        // the requesterId and role to the Service layer for ownership verification!
        return ResponseEntity.ok(sessionService.getFarmerHistory(farmerId));
    }

    @GetMapping("/reports/usage")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager')")
    public ResponseEntity<List<Map<String, Object>>> getUsageReport() {
        return ResponseEntity.ok(sessionService.getUsageAnalytics());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<AdvisorySessionResponseDTO> getAdvisoryById(@PathVariable Long id) {
        // Fetch the data from the service layer
        AdvisorySessionResponseDTO responseDTO = sessionService.getAdvisoryById(id);

        // Return 200 OK with the body
        return ResponseEntity.ok(responseDTO);
    }
}