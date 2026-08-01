package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.enums.ApplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendStatusUpdateNotification(String studentEmail, String studentName,
                                             String courseName, ApplicationStatus newStatus) {

        // Simulated delay to represent a real email service being slow
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("=== EMAIL NOTIFICATION SENT ===");
        logger.info("To: {}", studentEmail);
        logger.info("Subject: Your application status has been updated");
        logger.info("Dear {}, your application for {} has been updated to: {}",
                studentName, courseName, newStatus);
        logger.info("================================");
    }
}