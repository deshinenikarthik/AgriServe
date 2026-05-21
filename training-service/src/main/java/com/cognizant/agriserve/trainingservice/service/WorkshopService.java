package com.cognizant.agriserve.trainingservice.service;

import com.cognizant.agriserve.trainingservice.dto.request.WorkshopRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.WorkshopResponseDTO;
import java.util.List;

public interface WorkshopService {

    List<WorkshopResponseDTO> getAllWorkshops();

    List<WorkshopResponseDTO> getActiveWorkshopsForFarmers();

    // SECURITY: Added requesterId and isAdmin
    WorkshopResponseDTO scheduleWorkshop(WorkshopRequestDTO requestDto, Long requesterId, String role);

    List<WorkshopResponseDTO> getWorkshopsByOfficer(Long officerId);

    // SECURITY: Added requesterId and isAdmin
    WorkshopResponseDTO updateWorkshopStatus(Long workshopId, String status, Long requesterId, String role);

    // SECURITY: Added requesterId and isAdmin
    WorkshopResponseDTO updateWorkshop(Long workshopId, WorkshopRequestDTO requestDto, Long requesterId, String role);

    List<WorkshopResponseDTO> getWorkshopsByProgram(Long programId);

    // SECURITY: Added requesterId and isAdmin
    void deleteWorkshop(Long workshopId, Long requesterId, String role);

    WorkshopResponseDTO getWorkshopById(Long workshopId);
}