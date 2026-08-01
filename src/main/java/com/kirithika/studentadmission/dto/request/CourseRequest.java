package com.kirithika.studentadmission.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be positive")
    private Integer totalSeats;

    @NotNull(message = "Minimum percentage is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private Double minimumPercentage;

    @NotNull(message = "Application fee is required")
    @PositiveOrZero(message = "Application fee cannot be negative")
    private Double applicationFee;
}