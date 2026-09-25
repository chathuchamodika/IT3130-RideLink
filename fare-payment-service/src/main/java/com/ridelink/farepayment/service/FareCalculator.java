package com.ridelink.farepayment.service;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.ridelink.farepayment.domain.FareBreakdown;

public interface FareCalculator {
    FareBreakdown calculate(BigDecimal distanceKm, int durationMinutes, LocalTime rideStartTime);

    /**
     * DOCUMENTED FARE RULE (defaults in FareProperties, overridable via ridelink.fare.*):
     *   subtotal       = baseFare + (ratePerKm x distanceKm) + (ratePerMinute x durationMinutes)
     *   nightSurcharge = 10% of subtotal, if the ride started between 22:00 and 05:59
     *   total          = max(subtotal + nightSurcharge, minimumFare)
     * All amounts are rounded HALF_UP to two decimal places.
     */
    FareBreakdown calculate1(BigDecimal distanceKm, int durationMinutes, LocalTime rideStartTime);
}