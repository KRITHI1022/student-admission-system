package com.kirithika.studentadmission.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String courseName;
    private Integer duration;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double minimumPercentage;
    private Double applicationFee;
}