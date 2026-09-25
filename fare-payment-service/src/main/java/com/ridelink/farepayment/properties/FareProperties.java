package com.ridelink.farepayment.properties;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ConfigurationProperties(prefix = "ridelink.fare")
public class FareProperties {

    /** Fixed charge added to every fare (LKR). */
    private BigDecimal baseFare = new BigDecimal("150.00");
    /** Charge per kilometre travelled (LKR). */
    private BigDecimal ratePerKm = new BigDecimal("100.00");
    /** Charge per minute of ride time (LKR). */
    private BigDecimal ratePerMinute = new BigDecimal("10.00");
    /** Surcharge applied to the subtotal for night rides (22:00-05:59). */
    private BigDecimal nightSurchargeRate = new BigDecimal("0.10");
    private int nightStartHour = 22;
    private int nightEndHour = 6;
    /** Lowest amount ever charged for a ride (LKR). */
    private BigDecimal minimumFare = new BigDecimal("250.00");
    private String currency = "LKR";
    /** Average speed used to turn distance into an estimated duration. */
    private BigDecimal averageSpeedKmh = new BigDecimal("30.0");
}