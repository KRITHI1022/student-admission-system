package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.ApplicationRequest;
import com.kirithika.studentadmission.dto.response.ApplicationResponse;
import com.kirithika.studentadmission.entity.*;
import com.kirithika.studentadmission.exception.*;
import com.kirithika.studentadmission.repository.*;
import com.kirithika.studentadmission.service.interfaces.ApplicationService;
import org.springframework.stereotype.Service;
import com.kirithika.studentadmission.enums.ApplicationStatus;
import com.kirithika.studentadmission.dto.request.StatusUpdateRequest;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  StudentRepository studentRepository,
                                  CourseRepository courseRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ApplicationResponse applyForCourse(String studentEmail, ApplicationRequest request) {

        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (request.getTwelfthPercentage() < course.getMinimumPercentage()) {
            throw new EligibilityException(
                    "You do not meet the minimum eligibility of " + course.getMinimumPercentage()
                            + "% for " + course.getCourseName());
        }

        Application application = Application.builder()
                .student(student)
                .course(course)
                .tenthPercentage(request.getTenthPercentage())
                .twelfthPercentage(request.getTwelfthPercentage())
                .graduationPercentage(request.getGraduationPercentage())
                .build();

        Application saved = applicationRepository.save(application);

        return mapToResponse(saved);
    }

    @Override
    public List<ApplicationResponse> getMyApplications(String studentEmail) {

        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return applicationRepository.findByStudentId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getAllApplications(ApplicationStatus status) {

        List<Application> applications = (status != null)
                ? applicationRepository.findByStatus(status)
                : applicationRepository.findAll();

        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationResponse updateStatus(Long applicationId, StatusUpdateRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (request.getStatus() == ApplicationStatus.ADMITTED
                && application.getStatus() != ApplicationStatus.ADMITTED) {

            Course course = application.getCourse();

            if (course.getAvailableSeats() <= 0) {
                throw new EligibilityException("No seats available in " + course.getCourseName());
            }

            course.setAvailableSeats(course.getAvailableSeats() - 1);
            courseRepository.save(course);
        }

        application.setStatus(request.getStatus());
        application.setRemarks(request.getRemarks());

        Application updated = applicationRepository.save(application);
        notificationService.sendStatusUpdateNotification(
                application.getStudent().getUser().getEmail(),
                application.getStudent().getFullName(),
                application.getCourse().getCourseName(),
                application.getStatus()
        );

        return mapToResponse(updated);


    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .studentName(application.getStudent().getFullName())
                .courseName(application.getCourse().getCourseName())
                .status(application.getStatus())
                .remarks(application.getRemarks())
                .tenthPercentage(application.getTenthPercentage())
                .twelfthPercentage(application.getTwelfthPercentage())
                .graduationPercentage(application.getGraduationPercentage())
                .submittedAt(application.getSubmittedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    // Note: eligibility check comes in Phase 6 — right now, applying always succeeds
    // regardless of percentage vs course minimum. We'll add that check next phase.
}