package com.cognizant.agriserve.trainingservice.service.impl;

import com.cognizant.agriserve.trainingservice.client.UserClient;
import com.cognizant.agriserve.trainingservice.dao.TrainingProgramRepository;
import com.cognizant.agriserve.trainingservice.dao.WorkshopRepository;
import com.cognizant.agriserve.trainingservice.dto.request.WorkshopRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.WorkshopResponseDTO;
import com.cognizant.agriserve.trainingservice.entity.TrainingProgram;
import com.cognizant.agriserve.trainingservice.entity.Workshop;
import com.cognizant.agriserve.trainingservice.exception.ApiException;
import com.cognizant.agriserve.trainingservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.trainingservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.trainingservice.service.WorkshopService;
import feign.FeignException; // <-- Added for fault tolerance
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final TrainingProgramRepository programRepository;
    private final UserClient userClient;
    private final ModelMapper modelMapper;

    @Override
    public List<WorkshopResponseDTO> getAllWorkshops() {
        return workshopRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<WorkshopResponseDTO> getActiveWorkshopsForFarmers() {
        return workshopRepository.findAll().stream()
                .filter(w -> "Scheduled".equals(w.getStatus()) || "Ongoing".equals(w.getStatus()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public WorkshopResponseDTO scheduleWorkshop(WorkshopRequestDTO requestDto, Long requesterId, String role) {
        // 1. RBAC Check
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can schedule workshops.");
        }

        TrainingProgram program = programRepository.findById(requestDto.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Training Program", "ID", requestDto.getProgramId()));

        // 2. Ownership Check
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !program.getManagerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You cannot schedule a workshop for a program you did not create.");
        }

        // 3. Network-Resilient Cross-Service Check
        verifyOfficerExists(requestDto.getOfficerId());

        Workshop newWorkshop = modelMapper.map(requestDto, Workshop.class);
        newWorkshop.setTrainingProgram(program);
        newWorkshop.setStatus("Scheduled");

        Workshop savedWorkshop = workshopRepository.save(newWorkshop);
        return convertToDto(savedWorkshop);
    }

    @Override
    public List<WorkshopResponseDTO> getWorkshopsByOfficer(Long officerId) {
        return workshopRepository.findByOfficerId(officerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public WorkshopResponseDTO updateWorkshopStatus(Long workshopId, String status, Long requesterId, String role) {
        // 1. RBAC Check (Extension Officers included here)
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER") && !role.equalsIgnoreCase("EXTENSIONOFFICER"))) {
            throw new UnauthorizedActionException("Access Denied: You do not have permission to update workshop statuses.");
        }

        Workshop existingWorkshop = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop", "ID", workshopId));

        // 2. Ownership / Assignment Check
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        boolean isOwner = existingWorkshop.getTrainingProgram().getManagerId().equals(requesterId);
        boolean isAssignedOfficer = existingWorkshop.getOfficerId().equals(requesterId);

        if (!isAdmin && !isOwner && !isAssignedOfficer) {
            throw new UnauthorizedActionException("Access Denied: Only the program manager or the assigned extension officer can update this workshop status.");
        }

        existingWorkshop.setStatus(status);
        Workshop updatedWorkshop = workshopRepository.save(existingWorkshop);
        return convertToDto(updatedWorkshop);
    }

    @Override
    public WorkshopResponseDTO updateWorkshop(Long workshopId, WorkshopRequestDTO requestDto, Long requesterId, String role) {
        // 1. RBAC Check
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can edit workshops.");
        }

        Workshop existingWorkshop = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop", "ID", workshopId));

        // 2. Ownership Check
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !existingWorkshop.getTrainingProgram().getManagerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You cannot edit a workshop for a program you did not create.");
        }

        // 3. Network-Resilient Check (Only ping User Service if the officer ID actually changed)
        if (!existingWorkshop.getOfficerId().equals(requestDto.getOfficerId())) {
            verifyOfficerExists(requestDto.getOfficerId());
        }

        existingWorkshop.setLocation(requestDto.getLocation());
        existingWorkshop.setDate(requestDto.getDate());
        existingWorkshop.setOfficerId(requestDto.getOfficerId());

        Workshop updatedWorkshop = workshopRepository.save(existingWorkshop);
        return convertToDto(updatedWorkshop);
    }

    @Override
    public List<WorkshopResponseDTO> getWorkshopsByProgram(Long programId) {
        return workshopRepository.findByTrainingProgram_ProgramId(programId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteWorkshop(Long workshopId, Long requesterId, String role) {
        // 1. RBAC Check
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can delete workshops.");
        }

        Workshop existingWorkshop = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop", "ID", workshopId));

        // 2. Ownership Check
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !existingWorkshop.getTrainingProgram().getManagerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You cannot delete a workshop for a program you did not create.");
        }

        workshopRepository.delete(existingWorkshop);
    }

    @Override
    public WorkshopResponseDTO getWorkshopById(Long workshopId) {
        Workshop workshop = workshopRepository.findById(workshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Workshop", "ID", workshopId));
        return convertToDto(workshop);
    }

    // --- Helper Methods ---

    private void verifyOfficerExists(Long officerId) {
        if (officerId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Officer ID cannot be null.");
        }
        try {
            if (!userClient.checkUserExists(officerId)) {
                throw new ResourceNotFoundException("Officer ID " + officerId + " not found in User System.");
            }
        } catch (FeignException e) {
            log.error("Failed to connect to User Service for officer ID {}: {}", officerId, e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "User verification service is currently offline. Please try again later.");
        }
    }

    private WorkshopResponseDTO convertToDto(Workshop workshop) {
        WorkshopResponseDTO dto = modelMapper.map(workshop, WorkshopResponseDTO.class);
        if (workshop.getTrainingProgram() != null) {
            dto.setProgramTitle(workshop.getTrainingProgram().getTitle());
            dto.setProgramId(workshop.getTrainingProgram().getProgramId());
        }
        return dto;
    }
}