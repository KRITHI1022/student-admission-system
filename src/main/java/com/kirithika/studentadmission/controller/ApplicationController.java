package com.kirithika.studentadmission.controller;

import com.kirithika.studentadmission.dto.request.ApplicationRequest;
import com.kirithika.studentadmission.dto.response.ApplicationResponse;
import com.kirithika.studentadmission.service.interfaces.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kirithika.studentadmission.enums.ApplicationStatus;
import com.kirithika.studentadmission.dto.request.StatusUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {

        String studentEmail = authentication.getName();
        ApplicationResponse response = applicationService.applyForCourse(studentEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            Authentication authentication) {

        String studentEmail = authentication.getName();
        return ResponseEntity.ok(applicationService.getMyApplications(studentEmail));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @RequestParam(required = false) ApplicationStatus status) {

        return ResponseEntity.ok(applicationService.getAllApplications(status));
    }

    @PutMapping("/admin/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody StatusUpdateRequest request) {

        return ResponseEntity.ok(applicationService.updateStatus(applicationId, request));
    }
}