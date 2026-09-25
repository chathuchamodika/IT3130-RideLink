package com.ridelink.farepayment.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FareBreakdown {
    private String currency;
    private BigDecimal baseFare;
    private BigDecimal distanceKm;
    private BigDecimal distanceCharge;
    private Integer durationMinutes;
    private BigDecimal timeCharge;
    private BigDecimal nightSurcharge;
    private BigDecimal total;
}