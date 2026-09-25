package com.ridelink.farepayment.exception;

public class ReceiptNotFoundException extends RuntimeException {
    public ReceiptNotFoundException(String message) { super("Receipt not found: " + message); }
}