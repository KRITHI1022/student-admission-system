package com.kirithika.studentadmission.service.interfaces;

import com.kirithika.studentadmission.dto.request.PaymentVerificationRequest;
import com.kirithika.studentadmission.dto.response.PaymentOrderResponse;

public interface PaymentService {

    PaymentOrderResponse createOrder(Long applicationId, String studentEmail);

    void verifyPayment(PaymentVerificationRequest request);
}