package com.kirithika.studentadmission.repository;

import com.kirithika.studentadmission.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}