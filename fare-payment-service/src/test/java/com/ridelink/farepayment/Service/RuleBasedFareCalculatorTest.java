package com.ridelink.farepayment.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.ridelink.farepayment.domain.FareBreakdown;
import com.ridelink.farepayment.properties.FareProperties;
import com.ridelink.farepayment.service.RuleBasedFareCalculator;

class RuleBasedFareCalculatorTest {

    private final RuleBasedFareCalculator calculator =
            new RuleBasedFareCalculator(new FareProperties()); // defaults: 150 + 100/km + 10/min, 10% night, min 250

    @Test
    void daytimeFare_containsBaseDistanceAndTimeCharges() {
        FareBreakdown fare = calculator.calculate(new BigDecimal("5"), 10, LocalTime.of(10, 0));
        assertEquals(new BigDecimal("150.00"), fare.getBaseFare());
        assertEquals(new BigDecimal("500.00"), fare.getDistanceCharge());
        assertEquals(new BigDecimal("100.00"), fare.getTimeCharge());
        assertEquals(0, fare.getNightSurcharge().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("750.00"), fare.getTotal());
    }

    @Test
    void nightRide_at2230_appliesTenPercentSurcharge() {
        FareBreakdown fare = calculator.calculate(new BigDecimal("5"), 10, LocalTime.of(22, 30));
        assertEquals(new BigDecimal("75.00"), fare.getNightSurcharge());
        assertEquals(new BigDecimal("825.00"), fare.getTotal());
    }

    @Test
    void nightRide_at0559_appliesSurcharge() {
        FareBreakdown fare = calculator.calculate(new BigDecimal("5"), 10, LocalTime.of(5, 59));
        assertEquals(new BigDecimal("75.00"), fare.getNightSurcharge());
    }

    @Test
    void boundary_at0600_noSurcharge() {
        assertEquals(0, calculator.calculate(new BigDecimal("5"), 10, LocalTime.of(6, 0))
                .getNightSurcharge().compareTo(BigDecimal.ZERO));
    }

    @Test
    void boundary_at2159_noSurcharge() {
        assertEquals(0, calculator.calculate(new BigDecimal("5"), 10, LocalTime.of(21, 59))
                .getNightSurcharge().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shortRide_chargesAtLeastMinimumFare() {
        FareBreakdown fare = calculator.calculate(new BigDecimal("0.5"), 1, LocalTime.of(12, 0));
        assertEquals(new BigDecimal("250.00"), fare.getTotal());
    }

    @Test
    void amounts_areRoundedHalfUpToTwoDecimals() {
        FareBreakdown fare = calculator.calculate(new BigDecimal("3.333"), 7, LocalTime.of(12, 0));
        assertEquals(new BigDecimal("333.30"), fare.getDistanceCharge());
        assertEquals(new BigDecimal("553.30"), fare.getTotal());
    }

    @Test
    void negativeDistance_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("-1"), 10, LocalTime.NOON));
    }

    @Test
    void nullArguments_areRejected() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculate(null, 10, LocalTime.NOON));
        assertThrows(NullPointerException.class,
                () -> calculator.calculate(new BigDecimal("5"), 10, null));
    }
}