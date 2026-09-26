package com.ridelink.farepayment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Simulated distance calculation (no external map service required by the brief). */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoUtils() {}

    public static BigDecimal haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(EARTH_RADIUS_KM * c).setScale(2, RoundingMode.HALF_UP);
    }
}