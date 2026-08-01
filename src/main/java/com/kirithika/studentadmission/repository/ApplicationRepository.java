package com.kirithika.studentadmission.repository;

import com.kirithika.studentadmission.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kirithika.studentadmission.enums.ApplicationStatus;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);

    List<Application> findByCourseId(Long courseId);

    List<Application> findByStatus(ApplicationStatus status);
}