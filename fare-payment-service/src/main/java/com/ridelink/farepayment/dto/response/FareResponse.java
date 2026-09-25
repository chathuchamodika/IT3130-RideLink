package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ridelink.farepayment.domain.FareBreakdown;
import com.ridelink.farepayment.domain.FareRecord;

public record FareResponse(String rideId, String passengerId, String driverId, BigDecimal distanceKm,
                           Integer durationMinutes, FareBreakdownView fare, LocalDateTime calculatedAt) {

    public static FareResponse from(FareRecord r) {
        return new FareResponse(r.getRideId(), r.getPassengerId(), r.getDriverId(), r.getDistanceKm(),
                r.getDurationMinutes(), FareBreakdownView.from((FareBreakdown) r.getBreakdown()), r.getCalculatedAt());
    }
}