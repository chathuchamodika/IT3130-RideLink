package com.ridelink.farepayment.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FinalFareRequest(
        @NotBlank @Size(max = 64) String rideId,
        @NotBlank String passengerId,
        String driverId,
        @NotNull @DecimalMin(value = "0.1", message = "distance must be at least 0.1 km")
        @Digits(integer = 4, fraction = 2) BigDecimal distanceKm,
        @NotNull @DecimalMin(value = "1", message = "duration must be at least 1 minute")
        @Digits(integer = 4, fraction = 0) Integer durationMinutes,
        LocalDateTime completedAt) {}   // optional; defaults to "now" (used for the night rule)