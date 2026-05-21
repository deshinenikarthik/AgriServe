package com.cognizant.agriserve.trainingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopResponseDTO {

    private Long workshopId;
    private Long programId;
    private String programTitle;
    private String title;
    private Long officerId;
    private String location;
    private LocalDateTime date;
    private String status;
}
