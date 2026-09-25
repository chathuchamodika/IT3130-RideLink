package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;

public record FareEstimateResponse(BigDecimal distanceKm, int estimatedDurationMinutes,
                                   FareBreakdownView fare) {}