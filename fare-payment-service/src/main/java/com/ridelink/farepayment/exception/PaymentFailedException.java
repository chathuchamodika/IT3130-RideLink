package com.ridelink.farepayment.exception;

import lombok.Getter;

public class PaymentFailedException extends RuntimeException {
    @Getter private final String paymentId;
    public PaymentFailedException(String paymentId, String reason) {
        super("Payment failed: " + reason);
        this.paymentId = paymentId;
    }
}