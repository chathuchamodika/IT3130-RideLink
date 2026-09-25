package com.ridelink.farepayment.service;

import java.math.BigDecimal;

import com.ridelink.farepayment.domain.PaymentMethod;

public interface PaymentGateway {
    GatewayResult process(PaymentMethod method, BigDecimal amount, String currency, boolean simulateFailure);

    public class GatewayResult {

        public static GatewayResult failure(String string) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'failure'");
        }

        public static GatewayResult success() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'success'");
        }
    }
}