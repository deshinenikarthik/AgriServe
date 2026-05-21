package com.cognizant.agriserve.trainingservice.service.impl;

import com.cognizant.agriserve.trainingservice.client.UserClient;
import com.cognizant.agriserve.trainingservice.dao.TrainingProgramRepository;
import com.cognizant.agriserve.trainingservice.dto.request.TrainingProgramRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.TrainingProgramResponseDTO;
import com.cognizant.agriserve.trainingservice.entity.TrainingProgram;
import com.cognizant.agriserve.trainingservice.exception.ApiException;
import com.cognizant.agriserve.trainingservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.trainingservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.trainingservice.service.TrainingProgramService;
import feign.FeignException;
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
public class TrainingProgramServiceImpl implements TrainingProgramService {

    private final TrainingProgramRepository programRepository;
    private final UserClient userClient;
    private final ModelMapper modelMapper;

    @Override
    public TrainingProgramResponseDTO createProgram(TrainingProgramRequestDTO requestDto, String role) {
        log.info("Validating business rules for new Training Program...");

        // 1. ROLE-BASED ACCESS CONTROL
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can create training programs.");
        }

        if (requestDto.getManagerId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Manager ID cannot be null when creating a program.");
        }
        validateDates(requestDto);

        // 2. Cross-Microservice Validation with Fault Tolerance
        boolean managerExists;
        try {
            managerExists = userClient.checkUserExists(requestDto.getManagerId());
        } catch (FeignException e) {
            log.error("Failed to connect to User Service for manager ID {}: {}", requestDto.getManagerId(), e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "User verification service is currently offline. Please try again later.");
        }

        if (!managerExists) {
            throw new ResourceNotFoundException("Manager ID " + requestDto.getManagerId() + " does not exist in the User System.");
        }

        TrainingProgram newProgram = modelMapper.map(requestDto, TrainingProgram.class);
        newProgram.setManagerId(requestDto.getManagerId());
        newProgram.setStatus("Scheduled");

        log.info("Saving Training Program to database...");
        TrainingProgram savedProgram = programRepository.save(newProgram);

        return modelMapper.map(savedProgram, TrainingProgramResponseDTO.class);
    }

    @Override
    public TrainingProgramResponseDTO updateProgram(Long programId, TrainingProgramRequestDTO requestDto, Long requesterId, String role) {
        // 1. ROLE-BASED ACCESS CONTROL
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can modify training programs.");
        }

        TrainingProgram existingProgram = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Training Program", "ID", programId));

        // 2. OWNERSHIP VERIFICATION (Admins bypass this)
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !existingProgram.getManagerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You are not authorized to edit a training program you did not create.");
        }

        validateDates(requestDto);

        existingProgram.setTitle(requestDto.getTitle());
        existingProgram.setDescription(requestDto.getDescription());
        existingProgram.setStartDate(requestDto.getStartDate());
        existingProgram.setEndDate(requestDto.getEndDate());
        existingProgram.setStatus(requestDto.getStatus());

        TrainingProgram updatedProgram = programRepository.save(existingProgram);
        return modelMapper.map(updatedProgram, TrainingProgramResponseDTO.class);
    }

    @Override
    public void deleteProgram(Long programId, Long requesterId, String role) {
        // 1. ROLE-BASED ACCESS CONTROL
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("PROGRAMMANAGER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Program Managers can delete training programs.");
        }

        TrainingProgram existingProgram = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Training Program", "ID", programId));

        // 2. OWNERSHIP VERIFICATION (Admins bypass this)
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !existingProgram.getManagerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You are not authorized to delete a training program you did not create.");
        }

        programRepository.delete(existingProgram);
    }

    @Override
    public List<TrainingProgramResponseDTO> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(program -> modelMapper.map(program, TrainingProgramResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingProgramResponseDTO getProgramById(Long programId) {
        TrainingProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Training Program", "ID", programId));

        return modelMapper.map(program, TrainingProgramResponseDTO.class);
    }

    @Override
    public boolean checkProgramExists(Long programId) {
        return programRepository.existsById(programId);
    }

    @Override
    public List<TrainingProgramResponseDTO> getProgramsByStatus(String status) {
        return programRepository.findByStatus(status).stream()
                .map(program -> modelMapper.map(program, TrainingProgramResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingProgramResponseDTO getProgramForFeign(Long id) {
        log.debug("Fetching Training Program data for internal microservice. ID: {}", id);

        // 1. Fetch the Entity
        TrainingProgram program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training Program not found with ID: " + id));

        // 2. Map directly to DTO and return
        return modelMapper.map(program, TrainingProgramResponseDTO.class);

    }

    private void validateDates(TrainingProgramRequestDTO requestDto) {
        if (requestDto.getStartDate() != null && requestDto.getEndDate() != null && requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Program start date cannot be later than the end date.");
        }
    }
}