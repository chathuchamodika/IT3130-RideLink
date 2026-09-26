package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ridelink.farepayment.domain.Payment;
import com.ridelink.farepayment.domain.PaymentMethod;
import com.ridelink.farepayment.domain.PaymentStatus;

public record PaymentResponse(String paymentId, String rideId, String passengerId, String driverId,
                              BigDecimal amount, String currency, PaymentMethod method, PaymentStatus status,
                              String failureReason, int attempts, FareBreakdownView fare,
                              ReceiptResponse receipt, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getRideId(), p.getPassengerId(), p.getDriverId(),
                p.getAmount(), p.getCurrency(), p.getMethod(), p.getStatus(), p.getFailureReason(),
                p.getAttempts(), p.getFare() == null ? null : FareBreakdownView.from(p.getFare()),
                p.getReceipt() == null ? null : ReceiptResponse.from(p),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}