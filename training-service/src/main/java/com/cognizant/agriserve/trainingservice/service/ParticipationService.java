package com.cognizant.agriserve.trainingservice.service;

import com.cognizant.agriserve.trainingservice.dto.request.AttendanceUpdateRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.request.ParticipationRequestDTO;
import com.cognizant.agriserve.trainingservice.dto.response.ParticipationResponseDTO;
import java.util.List;

public interface ParticipationService {

    ParticipationResponseDTO registerForWorkshop(ParticipationRequestDTO requestDto, String role);

    List<ParticipationResponseDTO> getParticipantsForWorkshop(Long workshopId);

    // SECURITY: Added requesterId and isAdmin
    ParticipationResponseDTO updateAttendance(AttendanceUpdateRequestDTO requestDto, Long requesterId, String role);

    List<ParticipationResponseDTO> getParticipationByFarmerId(Long farmerId);
}