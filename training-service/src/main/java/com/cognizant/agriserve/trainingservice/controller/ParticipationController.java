package com.cognizant.agriserve.trainingservice.controller;

import com.cognizant.agriserve.trainingservice.client.FarmerClient;
import com.cognizant.agriserve.trainingservice.dto.request.AttendanceUpdateRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.request.ParticipationRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.FarmerResponseDTO;
import com.cognizant.agriserve.trainingservice.dto.response.ParticipationResponseDTO;
import com.cognizant.agriserve.trainingservice.dto.response.WorkshopResponseDTO;
import com.cognizant.agriserve.trainingservice.service.ParticipationService;
import com.cognizant.agriserve.trainingservice.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/participations")
public class ParticipationController {

    private final ParticipationService participationService;
    private final FarmerClient farmerClient;
    private final WorkshopService workshopService; // 👈 Add this

    public ParticipationController(ParticipationService participationService, FarmerClient farmerClient, WorkshopService workshopService) {
        this.participationService = participationService;
        this.farmerClient =  farmerClient;
        this.workshopService = workshopService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<ParticipationResponseDTO> registerForWorkshop(
            @RequestHeader("X-Logged-In-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody ParticipationRequestDTO requestDto) {

        // 1. Fetch Farmer details from farmer-service using the userId
        FarmerResponseDTO farmer = farmerClient.getFarmerByUserId(userId);

        // 2. Extract farmerId and update the DTO
        Long fetchedFarmerId = farmer.getFarmerId();
        requestDto.setFarmerId(fetchedFarmerId);

        log.info("User [ID={}] identified as Farmer [ID={}]. Registering for Workshop ID: {}",
                userId, fetchedFarmerId, requestDto.getWorkshopId());

        // 3. Pass to service
        return new ResponseEntity<>(
                participationService.registerForWorkshop(requestDto, role),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/workshop/{workshopId}")
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'ProgramManager', 'Admin')")
    public ResponseEntity<List<ParticipationResponseDTO>> getParticipantsForWorkshop(
            @PathVariable Long workshopId) {

        return ResponseEntity.ok(participationService.getParticipantsForWorkshop(workshopId));
    }

    @GetMapping("/farmer/{farmerId}")
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'ProgramManager', 'Admin')")
    public ResponseEntity<List<ParticipationResponseDTO>> getParticipationByFarmerId(
            @PathVariable Long farmerId) {

        return ResponseEntity.ok(participationService.getParticipationByFarmerId(farmerId));
    }

    @PutMapping("/attendance")
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'Admin')")
    public ResponseEntity<ParticipationResponseDTO> updateAttendance(
            @RequestHeader("X-Logged-In-User-Id") Long officerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody AttendanceUpdateRequestDTO requestDto) {

        log.info("User [ID={}] with role [{}] updating attendance for Participation ID: {}", officerId, role, requestDto.getParticipationId());

        // Pass the role directly as a String
        return ResponseEntity.ok(participationService.updateAttendance(requestDto, officerId, role));
    }

    @GetMapping("/my-workshops")
    @PreAuthorize("hasRole('Farmer')")
    public ResponseEntity<List<WorkshopResponseDTO>> getMyRegisteredWorkshops( // 👈 Changed return type
                                                                               @RequestHeader("X-Logged-In-User-Id") Long userId,
                                                                               @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        log.info("Fetching registered workshops for logged-in User ID: {}", userId);

        // 1. Fetch Farmer details from farmer-service using the userId
        FarmerResponseDTO farmer = farmerClient.getFarmerByUserId(userId);

        if (farmer == null || farmer.getFarmerId() == null) {
            log.error("No farmer profile found for User ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 2. Fetch participations (registered workshops) using the extracted farmerId
        List<ParticipationResponseDTO> myParticipations = participationService.getParticipationByFarmerId(farmer.getFarmerId());

        // 3. 👈 NEW: Map the participations to their full Workshop details
        List<WorkshopResponseDTO> registeredWorkshops = myParticipations.stream()
                .map(participation -> workshopService.getWorkshopById(participation.getWorkshopId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(registeredWorkshops);
    }
}