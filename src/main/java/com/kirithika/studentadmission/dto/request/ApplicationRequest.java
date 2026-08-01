package com.kirithika.studentadmission.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "10th percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    private Double tenthPercentage;

    @NotNull(message = "12th percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    private Double twelfthPercentage;

    private Double graduationPercentage;
}