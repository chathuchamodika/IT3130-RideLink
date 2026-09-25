package com.ridelink.farepayment.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String rideId) { super("Ride " + rideId + " has already been paid."); }
}
