package com.ridelink.farepayment.service;

public record GatewayResult(boolean successful, String failureReason) {
    public static GatewayResult success() { return new GatewayResult(true, null); }
    public static GatewayResult failure(String reason) { return new GatewayResult(false, reason); }
}