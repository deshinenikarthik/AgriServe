package com.cognizant.agriserve.trainingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDTO {

    @NotNull(message = "Workshop ID is mandatory")
    private Long workshopId;

    private Long farmerId;
}
