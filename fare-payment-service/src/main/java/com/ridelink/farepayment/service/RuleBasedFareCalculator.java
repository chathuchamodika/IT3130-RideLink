package com.ridelink.farepayment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ridelink.farepayment.domain.FareBreakdown;
import com.ridelink.farepayment.properties.FareProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuleBasedFareCalculator implements FareCalculator {

    private final FareProperties properties;

    /**
     * DOCUMENTED FARE RULE (defaults in FareProperties, overridable via ridelink.fare.*):
     *   subtotal       = baseFare + (ratePerKm x distanceKm) + (ratePerMinute x durationMinutes)
     *   nightSurcharge = 10% of subtotal, if the ride started between 22:00 and 05:59
     *   total          = max(subtotal + nightSurcharge, minimumFare)
     * All amounts are rounded HALF_UP to two decimal places.
     */
    @Override
    public FareBreakdown calculate1(BigDecimal distanceKm, int durationMinutes, LocalTime rideStartTime) {
        Objects.requireNonNull(distanceKm, "distanceKm must not be null");
        Objects.requireNonNull(rideStartTime, "rideStartTime must not be null");
        if (distanceKm.signum() < 0) throw new IllegalArgumentException("distanceKm must not be negative");
        if (durationMinutes < 0) throw new IllegalArgumentException("durationMinutes must not be negative");

        BigDecimal baseFare = money(properties.getBaseFare());
        BigDecimal distanceCharge = money(properties.getRatePerKm().multiply(distanceKm));
        BigDecimal timeCharge = money(properties.getRatePerMinute().multiply(BigDecimal.valueOf(durationMinutes)));
        BigDecimal subtotal = baseFare.add(distanceCharge).add(timeCharge);
        BigDecimal nightSurcharge = isNight(rideStartTime)
                ? money(subtotal.multiply(properties.getNightSurchargeRate()))
                : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(nightSurcharge).max(money(properties.getMinimumFare()));

        return FareBreakdown.builder()
                .currency(properties.getCurrency())
                .baseFare(baseFare)
                .distanceKm(distanceKm.setScale(2, RoundingMode.HALF_UP))
                .distanceCharge(distanceCharge)
                .durationMinutes(durationMinutes)
                .timeCharge(timeCharge)
                .nightSurcharge(nightSurcharge)
                .total(total)
                .build();
    }

    private boolean isNight(LocalTime time) {
        int hour = time.getHour();
        return hour >= properties.getNightStartHour() || hour < properties.getNightEndHour();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public FareBreakdown calculate(BigDecimal distanceKm, int durationMinutes, LocalTime rideStartTime) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculate'");
    }

}