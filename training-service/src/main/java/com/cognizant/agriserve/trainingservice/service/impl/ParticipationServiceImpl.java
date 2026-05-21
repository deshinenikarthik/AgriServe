package com.cognizant.agriserve.trainingservice.service.impl;

import com.cognizant.agriserve.trainingservice.client.UserClient;
import com.cognizant.agriserve.trainingservice.dao.ParticipationRepository;
import com.cognizant.agriserve.trainingservice.dao.WorkshopRepository;
import com.cognizant.agriserve.trainingservice.dto.request.AttendanceUpdateRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.request.ParticipationRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.ParticipationResponseDTO;
import com.cognizant.agriserve.trainingservice.entity.Participation;
import com.cognizant.agriserve.trainingservice.entity.Workshop;
import com.cognizant.agriserve.trainingservice.exception.ApiException;
import com.cognizant.agriserve.trainingservice.exception.ResourceConflictException;
import com.cognizant.agriserve.trainingservice.exception.ResourceNotFoundException;
import com.cognizant.agriserve.trainingservice.exception.UnauthorizedActionException;
import com.cognizant.agriserve.trainingservice.service.ParticipationService;
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
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final WorkshopRepository workshopRepository;
    private final UserClient userClient;
    private final ModelMapper modelMapper;

    @Override
    public ParticipationResponseDTO registerForWorkshop(ParticipationRequestDTO requestDto, String role) {
        // 1. RBAC Check (Only Farmers can register themselves)
        if (role == null || !role.equalsIgnoreCase("FARMER")) {
            throw new UnauthorizedActionException("Access Denied: Only registered Farmers can enroll in workshops.");
        }

        Workshop workshop = workshopRepository.findById(requestDto.getWorkshopId())
                .orElseThrow(() -> new ResourceNotFoundException("Workshop", "ID", requestDto.getWorkshopId()));

        // 2. Network-Resilient Check (Optional now, as Feign Client just verified they exist)
        verifyFarmerExists(requestDto.getFarmerId());

        boolean alreadyRegistered = participationRepository
                .existsByWorkshop_WorkshopIdAndFarmerId(requestDto.getWorkshopId(), requestDto.getFarmerId());

        if (alreadyRegistered) {
            throw new ResourceConflictException("Farmer is already registered for this workshop.");
        }

        Participation newRegistration = new Participation();
        newRegistration.setWorkshop(workshop);
        newRegistration.setFarmerId(requestDto.getFarmerId());
        newRegistration.setAttendanceStatus("Registered");

        Participation savedRegistration = participationRepository.save(newRegistration);

        return convertToDto(savedRegistration);
    }

    @Override
    public List<ParticipationResponseDTO> getParticipantsForWorkshop(Long workshopId) {
        return participationRepository.findByWorkshop_WorkshopId(workshopId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationResponseDTO updateAttendance(AttendanceUpdateRequestDTO requestDto, Long requesterId, String role) {
        // 1. RBAC Check
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("EXTENSIONOFFICER"))) {
            throw new UnauthorizedActionException("Access Denied: Only Administrators and Extension Officers can update attendance.");
        }

        Participation existingRecord = participationRepository.findById(requestDto.getParticipationId())
                .orElseThrow(() -> new ResourceNotFoundException("Participation", "ID", requestDto.getParticipationId()));

        // 2. OWNERSHIP VERIFICATION: Only the officer assigned to this specific workshop can update attendance
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        if (!isAdmin && !existingRecord.getWorkshop().getOfficerId().equals(requesterId)) {
            throw new UnauthorizedActionException("Access Denied: You can only update attendance for workshops assigned to you.");
        }

        existingRecord.setAttendanceStatus(requestDto.getNewAttendanceStatus());
        Participation updatedRecord = participationRepository.save(existingRecord);

        return convertToDto(updatedRecord);
    }

    @Override
    public List<ParticipationResponseDTO> getParticipationByFarmerId(Long farmerId) {
        return participationRepository.findByFarmerId(farmerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---
    private void verifyFarmerExists(Long farmerId) {
        if (farmerId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Farmer ID cannot be null.");
        }
        try {
            if (!userClient.checkUserExists(farmerId)) {
                throw new ResourceNotFoundException("Farmer ID " + farmerId + " not found in User System.");
            }
        } catch (FeignException e) {
            log.error("Failed to connect to User Service for farmer ID {}: {}", farmerId, e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "User verification service is currently offline. Please try again later.");
        }
    }

    private ParticipationResponseDTO convertToDto(Participation participation) {
        ParticipationResponseDTO dto = modelMapper.map(participation, ParticipationResponseDTO.class);
        if (participation.getWorkshop() != null) {
            dto.setWorkshopId(participation.getWorkshop().getWorkshopId());
        }
        return dto;
    }
}