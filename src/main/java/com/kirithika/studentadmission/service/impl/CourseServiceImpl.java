package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.CourseRequest;
import com.kirithika.studentadmission.dto.response.CourseResponse;
import com.kirithika.studentadmission.entity.Course;
import com.kirithika.studentadmission.repository.CourseRepository;
import com.kirithika.studentadmission.service.interfaces.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Cacheable(value = "courses", key = "'all'")
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    @CacheEvict(value = "courses", key = "'all'")
    public CourseResponse createCourse(CourseRequest request) {

        Course course = Course.builder()
                .courseName(request.getCourseName())
                .duration(request.getDuration())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .minimumPercentage(request.getMinimumPercentage())
                .applicationFee(request.getApplicationFee())
                .build();

        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }



    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .duration(course.getDuration())
                .totalSeats(course.getTotalSeats())
                .availableSeats(course.getAvailableSeats())
                .minimumPercentage(course.getMinimumPercentage())
                .applicationFee(course.getApplicationFee())
                .build();
    }
}