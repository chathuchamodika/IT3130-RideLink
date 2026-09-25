package com.ridelink.farepayment.client;

import java.math.BigDecimal;

/** Expected shape of GET /rides/{rideId} - align field names with the Ride Management team. */
public record RideSummary(String rideId, String status, String passengerId, String driverId,
                          BigDecimal distanceKm, Integer durationMinutes) {}