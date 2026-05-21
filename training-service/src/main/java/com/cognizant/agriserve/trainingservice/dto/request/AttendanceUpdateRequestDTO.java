package com.cognizant.agriserve.trainingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequestDTO {

    @NotNull(message = "Participation ID is mandatory to update attendance")
    private Long participationId;

    @NotBlank(message = "New attendance status cannot be empty")
    @Pattern(regexp = "^(Present|Absent)$",
            message = "Status must be either 'Present' or 'Absent'")
    private String newAttendanceStatus;
}
