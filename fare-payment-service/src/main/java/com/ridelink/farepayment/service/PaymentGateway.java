package com.ridelink.farepayment.service;

import java.math.BigDecimal;

import com.ridelink.farepayment.domain.PaymentMethod;

public interface PaymentGateway {
    GatewayResult process(PaymentMethod method, BigDecimal amount, String currency, boolean simulateFailure);
}