package com.cognizant.agriserve.advisoryservice.controller;

import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentRequestDTO;
import com.cognizant.agriserve.advisoryservice.dto.AdvisoryContentResponseDTO;
import com.cognizant.agriserve.advisoryservice.service.AdvisoryContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisory-content")
@RequiredArgsConstructor
@Validated
public class AdvisoryContentController {

    private final AdvisoryContentService contentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager')")
    public ResponseEntity<AdvisoryContentResponseDTO> uploadContent(
            @RequestHeader("X-Logged-In-User-Id") Long uploaderId,
            @Valid @RequestBody AdvisoryContentRequestDTO requestDto) {

        return new ResponseEntity<>(contentService.saveContent(requestDto, uploaderId), HttpStatus.CREATED);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager', 'Farmer', 'ExtensionOfficer')")
    public ResponseEntity<List<AdvisoryContentResponseDTO>> getActiveContent() {
        return ResponseEntity.ok(contentService.getAllActiveContent());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'ProgramManager')")
    public ResponseEntity<Void> removeContent(
            @PathVariable @Min(value = 1, message = "ID must be positive") Long id,
            @RequestHeader("X-Logged-In-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        boolean isAdmin = role.equalsIgnoreCase("Admin");

        contentService.softDeleteContent(id, requesterId, isAdmin);

        return ResponseEntity.noContent().build();
    }
}