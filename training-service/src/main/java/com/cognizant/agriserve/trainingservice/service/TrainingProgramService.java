package com.cognizant.agriserve.trainingservice.service;

import com.cognizant.agriserve.trainingservice.dto.request.TrainingProgramRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.TrainingProgramResponseDTO;
import java.util.List;

public interface TrainingProgramService {

    TrainingProgramResponseDTO createProgram(TrainingProgramRequestDTO requestDto, String role);

    List<TrainingProgramResponseDTO> getAllPrograms();

    TrainingProgramResponseDTO getProgramById(Long programId);

    // SECURITY: Added requesterId and isAdmin
    TrainingProgramResponseDTO updateProgram(Long programId, TrainingProgramRequestDTO requestDto, Long requesterId, String role);

    // SECURITY: Added requesterId and isAdmin
    void deleteProgram(Long programId, Long requesterId, String role);

    boolean checkProgramExists(Long programId);

    List<TrainingProgramResponseDTO> getProgramsByStatus(String status);

    TrainingProgramResponseDTO getProgramForFeign(Long id);
}