package com.kirithika.studentadmission.controller;

import com.kirithika.studentadmission.dto.request.PaymentVerificationRequest;
import com.kirithika.studentadmission.dto.response.PaymentOrderResponse;
import com.kirithika.studentadmission.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order/{applicationId}")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @PathVariable Long applicationId,
            Authentication authentication) {

        String studentEmail = authentication.getName();
        return ResponseEntity.ok(paymentService.createOrder(applicationId, studentEmail));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        paymentService.verifyPayment(request);
        return ResponseEntity.ok("Payment verified successfully");
    }
}