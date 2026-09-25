package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;

import com.ridelink.farepayment.domain.FareBreakdown;

public record FareBreakdownView(String currency, BigDecimal baseFare, BigDecimal distanceKm,
                                 BigDecimal distanceCharge, Integer durationMinutes,
                                 BigDecimal timeCharge, BigDecimal nightSurcharge, BigDecimal total) {

    public static FareBreakdownView from(Object fare) {
        FareBreakdown object = (FareBreakdown) fare;
        return new FareBreakdownView(object.getCurrency(), object.getBaseFare(), object.getDistanceKm(),
            object.getDistanceCharge(), object.getDurationMinutes(), object.getTimeCharge(),
            object.getNightSurcharge(), object.getTotal());
    }
}