package com.ridelink.farepayment.exception;

public class FareNotFoundException extends RuntimeException {
    public FareNotFoundException(String rideId) {
        super("No final fare calculated for ride " + rideId + ". Call POST /api/v1/fares/final first.");
    }
}










