package com.ridelink.farepayment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FareEstimateRequest(
        @NotNull @Valid LocationPoint pickup,
        @NotNull @Valid LocationPoint destination) {}