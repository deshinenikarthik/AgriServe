package com.cognizant.agriserve.trainingservice.controller;

import com.cognizant.agriserve.trainingservice.dto.request.TrainingProgramRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.TrainingProgramResponseDTO;
import com.cognizant.agriserve.trainingservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.trainingservice.service.TrainingProgramService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/programs")
public class TrainingProgramController {

    private final TrainingProgramService programService;

    public TrainingProgramController(TrainingProgramService programService) {
        this.programService = programService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<TrainingProgramResponseDTO> createProgram(
            @RequestHeader("X-Logged-In-User-Id") Long managerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody TrainingProgramRequestDTO requestDto) {

        requestDto.setManagerId(managerId);

        log.info("User [ID={}] with Role [{}] creating Training Program: {}", managerId, role, requestDto.getTitle());

        // Pass the role to the service
        TrainingProgramResponseDTO savedProgram = programService.createProgram(requestDto, role);
        return new ResponseEntity<>(savedProgram, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'Farmer', 'ExtensionOfficer', 'ComplianceOfficer')")
    public ResponseEntity<List<TrainingProgramResponseDTO>> getAllPrograms() {
        log.info("getAllPrograms called");
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    @GetMapping("/{programId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<TrainingProgramResponseDTO> getProgramById(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getProgramById(programId));
    }

    @PutMapping("/{programId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<TrainingProgramResponseDTO> updateProgram(
            @PathVariable Long programId,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @Valid @RequestBody TrainingProgramRequestDTO requestDto) {

        log.info("User {} (Role: {}) is updating Training Program ID: {}", requesterId, role, programId);

        // Pass the role directly as a String instead of calculating boolean isAdmin
        return ResponseEntity.ok(programService.updateProgram(programId, requestDto, requesterId, role));
    }

    @DeleteMapping("/{programId}")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ExtensionOfficer')")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable Long programId,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        log.info("User {} (Role: {}) is deleting Training Program ID: {}", requesterId, role, programId);

        // Pass the role directly as a String
        programService.deleteProgram(programId, requesterId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{programId}/exists")
    @PreAuthorize("hasAnyRole('ComplianceOfficer', 'SERVICE')")
    public ResponseEntity<Void> checkProgramExists(@PathVariable Long programId) {
        boolean exists = programService.checkProgramExists(programId);
        if (exists) return ResponseEntity.ok().build();
        else throw new ResourceNotFoundException("Training Program", "ID", programId);
    }

    @GetMapping("/completed")
    @PreAuthorize("hasAnyRole('ProgramManager', 'Admin', 'ComplianceOfficer', 'SERVICE', 'ExtensionOfficer')")
    public ResponseEntity<List<TrainingProgramResponseDTO>> getCompletedPrograms() {
        return ResponseEntity.ok(programService.getProgramsByStatus("Completed"));
    }

    @GetMapping("/feigncall/{id}")
    @PreAuthorize("hasRole('SERVICE')")
    public TrainingProgramResponseDTO getProgramForFeign(@PathVariable("id") Long id) {
        log.info("Internal Feign call received for Training Program ID: {}", id);
        return programService.getProgramForFeign(id);
    }
}