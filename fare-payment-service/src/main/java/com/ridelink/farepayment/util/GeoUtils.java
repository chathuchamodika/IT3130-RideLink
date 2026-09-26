package com.ridelink.farepayment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GeoUtils {

	private static final double EARTH_RADIUS_KM = 6371.0;

	private GeoUtils() {
	}

	public static BigDecimal haversineKm(double pickupLatitude, double pickupLongitude,
										 double destinationLatitude, double destinationLongitude) {
		double latitudeDifference = Math.toRadians(destinationLatitude - pickupLatitude);
		double longitudeDifference = Math.toRadians(destinationLongitude - pickupLongitude);
		double latitude1 = Math.toRadians(pickupLatitude);
		double latitude2 = Math.toRadians(destinationLatitude);
		double haversine = Math.pow(Math.sin(latitudeDifference / 2), 2)
				+ Math.cos(latitude1) * Math.cos(latitude2)
				* Math.pow(Math.sin(longitudeDifference / 2), 2);
		double distance = 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(haversine));
		return BigDecimal.valueOf(distance).setScale(3, RoundingMode.HALF_UP);
	}
}
