package com.kirithika.studentadmission.service.interfaces;

import com.kirithika.studentadmission.dto.request.ApplicationRequest;
import com.kirithika.studentadmission.dto.response.ApplicationResponse;
import com.kirithika.studentadmission.dto.request.StatusUpdateRequest;
import com.kirithika.studentadmission.enums.ApplicationStatus;


import java.util.List;

public interface ApplicationService {

    ApplicationResponse applyForCourse(String studentEmail, ApplicationRequest request);

    List<ApplicationResponse> getMyApplications(String studentEmail);

    List<ApplicationResponse> getAllApplications(ApplicationStatus status);

    ApplicationResponse updateStatus(Long applicationId, StatusUpdateRequest request);
}