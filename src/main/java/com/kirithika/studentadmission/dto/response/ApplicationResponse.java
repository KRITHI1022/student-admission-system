package com.kirithika.studentadmission.dto.response;

import com.kirithika.studentadmission.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {

    private Long id;
    private String studentName;
    private String courseName;
    private ApplicationStatus status;
    private String remarks;
    private Double tenthPercentage;
    private Double twelfthPercentage;
    private Double graduationPercentage;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}