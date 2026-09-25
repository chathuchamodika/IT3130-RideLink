package com.ridelink.farepayment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ridelink.farepayment.domain.PaymentMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * All RideLink payments are simulated (brief §3). The simulateFailure flag exists
 * only to demonstrate the required "failed simulated payment" negative workflow.
 */
@Slf4j
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGateway.GatewayResult process(PaymentMethod method, BigDecimal amount, String currency,
                                 boolean simulateFailure) {
        if (simulateFailure) {
            log.info("Simulated gateway DECLINED {} {} via {}", amount, currency, method);
            return GatewayResult.failure("Simulated payment declined by payment gateway (test hook)");
        }
        log.info("Simulated gateway APPROVED {} {} via {}", amount, currency, method);
        return GatewayResult.success();
    }

}