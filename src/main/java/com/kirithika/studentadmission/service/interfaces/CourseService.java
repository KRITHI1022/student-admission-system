package com.kirithika.studentadmission.service.interfaces;

import com.kirithika.studentadmission.dto.request.CourseRequest;
import com.kirithika.studentadmission.dto.response.CourseResponse;

import java.util.List;

public interface CourseService {

    List<CourseResponse> getAllCourses();

    CourseResponse createCourse(CourseRequest request);
}