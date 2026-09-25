package com.ridelink.farepayment.dto.request;

import com.ridelink.farepayment.domain.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank String rideId,
        @NotNull PaymentMethod method,
        /** Test-only hook to demonstrate the required "failed simulated payment" workflow. */
        Boolean simulateFailure) {}