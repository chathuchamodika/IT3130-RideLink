package com.ridelink.farepayment.client;

import java.util.Optional;

/** Abstraction over the Ride Management Service so this service stays testable. 
 * @param <RideSummary>*/
@SuppressWarnings("hiding")
public interface RideClient<RideSummary> {
    /** Empty means "integration disabled" - no verification is performed. */
    Optional<RideSummary> fetchRide(String rideId);
}