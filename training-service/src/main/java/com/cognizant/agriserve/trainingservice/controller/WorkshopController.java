package com.cognizant.agriserve.trainingservice.controller;

import com.cognizant.agriserve.trainingservice.dto.request.WorkshopRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.WorkshopResponseDTO;
import com.cognizant.agriserve.trainingservice.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin')")
    public ResponseEntity<WorkshopResponseDTO> scheduleWorkshop(
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody WorkshopRequestDTO requestDto) {

        log.info("Manager [ID={}] with role [{}] scheduling a new workshop for Program ID: {}", requesterId, role, requestDto.getProgramId());
        WorkshopResponseDTO scheduledWorkshop = workshopService.scheduleWorkshop(requestDto, requesterId, role);
        return new ResponseEntity<>(scheduledWorkshop, HttpStatus.CREATED);
    }

    @PutMapping("/{workshopId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin')")
    public ResponseEntity<WorkshopResponseDTO> updateWorkshop(
            @PathVariable Long workshopId,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody WorkshopRequestDTO requestDto) {

        log.info("Manager [ID={}] with role [{}] requesting to edit Workshop ID: {}", requesterId, role, workshopId);
        return ResponseEntity.ok(workshopService.updateWorkshop(workshopId, requestDto, requesterId, role));
    }

    @PatchMapping("/{workshopId}/status")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<WorkshopResponseDTO> updateWorkshopStatus(
            @PathVariable Long workshopId,
            @RequestParam String status,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        log.info("User [ID={}] with role [{}] updating Workshop ID: {} to status: {}", requesterId, role, workshopId, status);
        return ResponseEntity.ok(workshopService.updateWorkshopStatus(workshopId, status, requesterId, role));
    }

    @DeleteMapping("/{workshopId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin')")
    public ResponseEntity<Void> deleteWorkshop(
            @PathVariable Long workshopId,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        log.info("Manager [ID={}] with role [{}] requesting to delete Workshop ID: {}", requesterId, role, workshopId);
        workshopService.deleteWorkshop(workshopId, requesterId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'Farmer')")
    public ResponseEntity<List<WorkshopResponseDTO>> getAllWorkshops() {
        return ResponseEntity.ok(workshopService.getAllWorkshops());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'Farmer')")
    public ResponseEntity<List<WorkshopResponseDTO>> getActiveWorkshops() {
        return ResponseEntity.ok(workshopService.getActiveWorkshopsForFarmers());
    }

    @GetMapping("/officer/{officerId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<List<WorkshopResponseDTO>> getWorkshopsByOfficer(@PathVariable Long officerId) {
        return ResponseEntity.ok(workshopService.getWorkshopsByOfficer(officerId));
    }

    @GetMapping("/program/{programId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer', 'Farmer')")
    public ResponseEntity<List<WorkshopResponseDTO>> getWorkshopsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(workshopService.getWorkshopsByProgram(programId));
    }

    @GetMapping("/{workshopId}")
    @PreAuthorize("hasAnyRole('ExtensionOfficer', 'ProgramManager', 'Admin', 'Farmer')")
    public ResponseEntity<WorkshopResponseDTO> getWorkshopById(
            @PathVariable Long workshopId) {

        log.info("Request received to fetch workshop with ID: {}", workshopId);

        WorkshopResponseDTO workshop = workshopService.getWorkshopById(workshopId);

        return ResponseEntity.ok(workshop);
    }
}