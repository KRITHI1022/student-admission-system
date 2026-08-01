package com.kirithika.studentadmission.dto.request;

import com.kirithika.studentadmission.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String remarks;
}