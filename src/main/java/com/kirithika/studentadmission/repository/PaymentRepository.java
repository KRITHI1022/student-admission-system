package com.kirithika.studentadmission.repository;

import com.kirithika.studentadmission.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByApplicationId(Long applicationId);

    Optional<Payment> findByTransactionId(String transactionId);
}