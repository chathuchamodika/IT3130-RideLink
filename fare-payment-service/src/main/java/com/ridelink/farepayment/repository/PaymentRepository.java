package com.ridelink.farepayment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.farepayment.domain.Payment;
import com.ridelink.farepayment.domain.PaymentStatus;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRideId(String rideId);
    Optional<Payment> findByReceiptReceiptNumber(String receiptNumber); // embedded field path
    List<Payment> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
    List<Payment> findByStatus(PaymentStatus status);
}