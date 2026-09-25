package com.ridelink.farepayment.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(prefix = "ridelink.ride-service", name = "enabled", havingValue = "true")
public class RestRideClient implements RideClient {

    private final RestClient restClient;

    public RestRideClient(RestClient.Builder builder,
                          @Value("${ridelink.ride-service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<RideSummary> fetchRide(String rideId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/rides/{rideId}", rideId)
                    .retrieve()
                    .body(RideSummary.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Ride service is unavailable", e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Ride service is unavailable", e);
        }
    }
}