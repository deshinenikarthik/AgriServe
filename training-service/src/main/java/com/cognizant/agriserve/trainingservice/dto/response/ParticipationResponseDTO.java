package com.cognizant.agriserve.trainingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationResponseDTO {

    private Long participationId;
    private Long workshopId;
    private Long farmerId;
    private String attendanceStatus;
    private String feedback;
}
