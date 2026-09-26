package com.ridelink.farepayment.client;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Used when ridelink.ride-service.enabled=false so the service can run/demo standalone. */
@Component
@ConditionalOnProperty(prefix = "ridelink.ride-service", name = "enabled",
                       havingValue = "false", matchIfMissing = true)
public class DisabledRideClient implements RideClient<RideSummary> {
    @Override
    public Optional<RideSummary> fetchRide(String rideId) {
        return Optional.empty();
    }
}
