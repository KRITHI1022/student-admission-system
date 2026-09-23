package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.ApplicationRequest;
import com.kirithika.studentadmission.dto.request.StatusUpdateRequest;
import com.kirithika.studentadmission.dto.response.ApplicationResponse;
import com.kirithika.studentadmission.entity.*;
import com.kirithika.studentadmission.enums.ApplicationStatus;
import com.kirithika.studentadmission.enums.PaymentStatus;
import com.kirithika.studentadmission.exception.*;
import com.kirithika.studentadmission.repository.*;
import com.kirithika.studentadmission.service.interfaces.ApplicationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService) {

        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    // ============================================================
    // STUDENT - APPLY FOR COURSE
    // ============================================================

    @Override
    public ApplicationResponse applyForCourse(
            String studentEmail,
            ApplicationRequest request) {

        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student profile not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found"));

        // Prevent duplicate applications
        if (applicationRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course.getId())) {

            throw new EligibilityException(
                    "You have already applied for "
                            + course.getCourseName());
        }

        // Validate 10th percentage
        if (request.getTenthPercentage() == null
                || request.getTenthPercentage() < 0
                || request.getTenthPercentage() > 100) {

            throw new EligibilityException(
                    "10th percentage must be between 0 and 100");
        }

        // Validate 12th percentage
        if (request.getTwelfthPercentage() == null
                || request.getTwelfthPercentage() < 0
                || request.getTwelfthPercentage() > 100) {

            throw new EligibilityException(
                    "12th percentage must be between 0 and 100");
        }

        // Check course eligibility
        if (request.getTwelfthPercentage()
                < course.getMinimumPercentage()) {

            throw new EligibilityException(
                    "You do not meet the minimum eligibility of "
                            + course.getMinimumPercentage()
                            + "% for "
                            + course.getCourseName());
        }

        // Check seat availability
        if (course.getAvailableSeats() <= 0) {

            throw new EligibilityException(
                    "No seats available in "
                            + course.getCourseName());
        }

        Application application = Application.builder()
                .student(student)
                .course(course)
                .tenthPercentage(
                        request.getTenthPercentage())
                .twelfthPercentage(
                        request.getTwelfthPercentage())
                .graduationPercentage(
                        request.getGraduationPercentage())
                .build();

        Application saved =
                applicationRepository.save(application);

        return mapToResponse(saved);
    }

    // ============================================================
    // STUDENT - GET MY APPLICATIONS
    // ============================================================

    @Override
    public List<ApplicationResponse> getMyApplications(
            String studentEmail) {

        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Student student =
                studentRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Student profile not found"));

        return applicationRepository
                .findByStudentId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ADMIN - GET ALL APPLICATIONS
    // ============================================================

    @Override
    public List<ApplicationResponse> getAllApplications(
            ApplicationStatus status) {

        List<Application> applications =
                (status != null)
                        ? applicationRepository.findByStatus(status)
                        : applicationRepository.findAll();

        return applications
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ADMIN - UPDATE APPLICATION STATUS
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(
            value = "courses",
            key = "'all'"
    )
    public ApplicationResponse updateStatus(
            Long applicationId,
            StatusUpdateRequest request) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        ApplicationStatus currentStatus =
                application.getStatus();

        ApplicationStatus newStatus =
                request.getStatus();

        // ========================================================
        // VALIDATE STATUS TRANSITION
        // ========================================================

        boolean validTransition = switch (currentStatus) {

            case SUBMITTED ->
                    newStatus == ApplicationStatus.UNDER_REVIEW
                            || newStatus == ApplicationStatus.REJECTED;

            case UNDER_REVIEW ->
                    newStatus == ApplicationStatus.APPROVED
                            || newStatus == ApplicationStatus.REJECTED;

            case APPROVED ->
                    newStatus == ApplicationStatus.ADMITTED;

            case REJECTED ->
                    false;

            case ADMITTED ->
                    false;
        };

        /*
         * Allow saving the same status without treating
         * it as an invalid transition.
         */
        if (currentStatus == newStatus) {
            validTransition = true;
        }

        if (!validTransition) {

            throw new EligibilityException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus);
        }

        // ========================================================
        // SEAT ALLOCATION
        // ========================================================

        /*
         * A seat is consumed only when the application
         * becomes ADMITTED.
         *
         * Because we check that the previous status was
         * not ADMITTED, the same application cannot consume
         * another seat.
         */

        if (newStatus == ApplicationStatus.ADMITTED
                && currentStatus != ApplicationStatus.ADMITTED) {

            Course course =
                    application.getCourse();

            if (course.getAvailableSeats() <= 0) {

                throw new EligibilityException(
                        "No seats available in "
                                + course.getCourseName());
            }

            course.setAvailableSeats(
                    course.getAvailableSeats() - 1);

            courseRepository.save(course);
        }

        // ========================================================
        // UPDATE APPLICATION
        // ========================================================

        application.setStatus(newStatus);
        application.setRemarks(
                request.getRemarks());

        Application updated =
                applicationRepository.save(application);

        // ========================================================
        // SEND STATUS NOTIFICATION
        // ========================================================

        notificationService.sendStatusUpdateNotification(
                application.getStudent()
                        .getUser()
                        .getEmail(),

                application.getStudent()
                        .getFullName(),

                application.getCourse()
                        .getCourseName(),

                application.getStatus()
        );

        return mapToResponse(updated);
    }

    // ============================================================
    // MAP ENTITY -> RESPONSE
    // ============================================================

    private ApplicationResponse mapToResponse(
            Application application) {

        return ApplicationResponse.builder()

                .id(application.getId())

                .studentName(
                        application.getStudent()
                                .getFullName())

                .courseName(
                        application.getCourse()
                                .getCourseName())

                .status(
                        application.getStatus())

                // Payment status
                .paymentStatus(
                        paymentRepository
                                .findByApplicationId(
                                        application.getId())
                                .map(Payment::getStatus)
                                .orElse(
                                        PaymentStatus.PENDING)
                )

                .remarks(
                        application.getRemarks())

                .tenthPercentage(
                        application.getTenthPercentage())

                .twelfthPercentage(
                        application.getTwelfthPercentage())

                .graduationPercentage(
                        application.getGraduationPercentage())

                .submittedAt(
                        application.getSubmittedAt())

                .updatedAt(
                        application.getUpdatedAt())

                .build();
    }
}