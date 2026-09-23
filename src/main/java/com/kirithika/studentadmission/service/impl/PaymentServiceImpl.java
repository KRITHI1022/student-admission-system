package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.PaymentVerificationRequest;
import com.kirithika.studentadmission.dto.response.PaymentOrderResponse;
import com.kirithika.studentadmission.entity.Application;
import com.kirithika.studentadmission.entity.Payment;
import com.kirithika.studentadmission.enums.PaymentStatus;
import com.kirithika.studentadmission.exception.EligibilityException;
import com.kirithika.studentadmission.exception.ResourceNotFoundException;
import com.kirithika.studentadmission.repository.ApplicationRepository;
import com.kirithika.studentadmission.repository.PaymentRepository;
import com.kirithika.studentadmission.service.interfaces.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            ApplicationRepository applicationRepository) {

        this.paymentRepository = paymentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public PaymentOrderResponse createOrder(
            Long applicationId,
            String studentEmail) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        /*
         * Make sure the logged-in student owns this application.
         */
        if (!application.getStudent()
                .getUser()
                .getEmail()
                .equals(studentEmail)) {

            throw new ResourceNotFoundException(
                    "Application not found");
        }

        Double amount =
                application.getCourse().getApplicationFee();

        try {

            RazorpayClient razorpayClient =
                    new RazorpayClient(
                            razorpayKeyId,
                            razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();

            orderRequest.put(
                    "amount",
                    (int) (amount * 100));

            orderRequest.put(
                    "currency",
                    "INR");

            orderRequest.put(
                    "receipt",
                    "app_" + applicationId);

            com.razorpay.Order order =
                    razorpayClient.orders.create(orderRequest);

            /*
             * Reuse the existing payment record if one exists.
             */
            Payment payment =
                    paymentRepository
                            .findByApplicationId(applicationId)
                            .orElse(
                                    Payment.builder()
                                            .application(application)
                                            .amount(amount)
                                            .paymentGateway("Razorpay")
                                            .build()
                            );

            /*
             * Store the Razorpay ORDER ID.
             *
             * This will later be compared with the
             * order ID received during verification.
             */
            payment.setTransactionId(order.get("id"));
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);

            return PaymentOrderResponse.builder()
                    .razorpayOrderId(order.get("id"))
                    .amount(amount)
                    .currency("INR")
                    .razorpayKeyId(razorpayKeyId)
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create payment order: "
                            + e.getMessage());
        }
    }

    @Override
    public void verifyPayment(
            PaymentVerificationRequest request,
            String studentEmail) {

        /*
         * Find the application first.
         */
        Application application =
                applicationRepository
                        .findById(request.getApplicationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        /*
         * SECURITY CHECK:
         * Make sure the logged-in student owns
         * the application being paid for.
         */
        if (!application.getStudent()
                .getUser()
                .getEmail()
                .equals(studentEmail)) {

            throw new ResourceNotFoundException(
                    "Application not found");
        }

        /*
         * Find the payment record belonging
         * to this application.
         */
        Payment payment =
                paymentRepository
                        .findByApplicationId(
                                request.getApplicationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment record not found"));

        /*
         * Prevent verification of an already
         * successful payment.
         */
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            throw new EligibilityException(
                    "Payment has already been completed");
        }

        /*
         * SECURITY CHECK:
         *
         * The Razorpay order ID received from the
         * frontend must match the order ID that
         * our backend created and stored.
         */
        if (payment.getTransactionId() == null
                || !payment.getTransactionId()
                .equals(request.getRazorpayOrderId())) {

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            throw new EligibilityException(
                    "Invalid Razorpay order");
        }

        try {

            JSONObject options = new JSONObject();

            options.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId());

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId());

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature());

            /*
             * Verify Razorpay's cryptographic signature
             * using the secret key stored on the backend.
             */
            boolean isValid =
                    Utils.verifyPaymentSignature(
                            options,
                            razorpayKeySecret);

            if (!isValid) {

                payment.setStatus(
                        PaymentStatus.FAILED);

                paymentRepository.save(payment);

                throw new EligibilityException(
                        "Payment signature verification failed");
            }

            /*
             * Payment is valid.
             */
            payment.setTransactionId(
                    request.getRazorpayPaymentId());

            payment.setStatus(
                    PaymentStatus.SUCCESS);

            payment.setPaidAt(
                    LocalDateTime.now());

            paymentRepository.save(payment);

        } catch (EligibilityException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Payment verification failed: "
                            + e.getMessage());
        }
    }
}