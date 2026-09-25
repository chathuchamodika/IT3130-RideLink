package com.ridelink.farepayment.exception;

public class RideNotCompletedException extends RuntimeException {
    public RideNotCompletedException(String rideId, String status) {
        super("Ride " + rideId + " is not COMPLETED (status: " + status + "), so it cannot be paid.");
    }
}

