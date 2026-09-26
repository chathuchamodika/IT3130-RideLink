package com.ridelink.farepayment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.client.RideSummary;
import com.ridelink.farepayment.domain.FareBreakdown;
import com.ridelink.farepayment.domain.FareRecord;
import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.FareEstimateRequest;
import com.ridelink.farepayment.dto.request.FinalFareRequest;
import com.ridelink.farepayment.dto.response.FareEstimateResponse;
import com.ridelink.farepayment.dto.response.FareBreakdownView;
import com.ridelink.farepayment.dto.response.FareResponse;
import com.ridelink.farepayment.exception.FareNotFoundException;
import com.ridelink.farepayment.exception.RideNotCompletedException;
import com.ridelink.farepayment.properties.FareProperties;
import com.ridelink.farepayment.util.GeoUtils;

import io.micrometer.core.instrument.Clock;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FareService {

    private final FareRecordRepository fareRecordRepository;
    private final FareCalculator fareCalculator;
    private final RideClient<RideSummary> rideClient;
    private final FareProperties fareProperties;
    private final Clock clock;

    /** Stateless estimate: Haversine distance -> assumed average speed -> fare rule. */
    public FareEstimateResponse estimate(FareEstimateRequest request) {
        BigDecimal distanceKm = GeoUtils.haversineKm(
                request.pickup().latitude(), request.pickup().longitude(),
                request.destination().latitude(), request.destination().longitude());
        int estimatedMinutes = estimatedDurationMinutes(distanceKm);
        FareBreakdown breakdown = fareCalculator.calculate(distanceKm, estimatedMinutes, currentTime());
        return new FareEstimateResponse(distanceKm, estimatedMinutes, FareBreakdownView.from(breakdown));
    }

    /** Final fare for a completed ride. Verifies ride status (if integration enabled) and is idempotent. */
    public FareCalculationOutcome calculateFinalFare(FinalFareRequest request) {
        rideClient.fetchRide(request.rideId()).ifPresent(ride -> {
            if (!"COMPLETED".equalsIgnoreCase(ride.status())) {
                throw new RideNotCompletedException(request.rideId(), ride.status());
            }
        });

        Optional<FareRecord> existing = fareRecordRepository.findByRideId(request.rideId());
        if (existing.isPresent()) {
            return new FareCalculationOutcome(FareResponse.from(existing.get()), false);
        }

        LocalTime rideTime = request.completedAt() != null
                ? request.completedAt().toLocalTime() : currentTime();
        FareBreakdown breakdown = fareCalculator.calculate(request.distanceKm(),
                request.durationMinutes(), rideTime);

        FareRecord saved = fareRecordRepository.save(FareRecord.builder()
                .rideId(request.rideId())
                .passengerId(request.passengerId())
                .driverId(request.driverId())
                .distanceKm(request.distanceKm())
                .durationMinutes(request.durationMinutes())
                .breakdown(breakdown)
                .calculatedAt(currentDateTime())
                .build());
        return new FareCalculationOutcome(FareResponse.from(saved), true);
    }

    public FareResponse getByRideId(String rideId, AuthenticatedUser requester) {
        FareRecord record = fareRecordRepository.findByRideId(rideId)
                .orElseThrow(() -> new FareNotFoundException(rideId));
        if (!canAccess(requester, record)) {
            throw new AccessDeniedException(
                    "Only the passenger, the driver of the ride or an admin can view this fare");
        }
        return FareResponse.from(record);
    }

    private boolean canAccess(AuthenticatedUser requester, FareRecord record) {
        return requester.admin()
                || requester.userId().equals(record.getPassengerId())
                || requester.userId().equals(record.getDriverId());
    }

    private int estimatedDurationMinutes(BigDecimal distanceKm) {
        BigDecimal hours = distanceKm.divide(fareProperties.getAverageSpeedKmh(), 4, RoundingMode.HALF_UP);
        return Math.max(1, (int) Math.ceil(hours.doubleValue() * 60));
    }

    private LocalTime currentTime() {
        return LocalTime.ofInstant(Instant.ofEpochMilli(clock.wallTime()), ZoneId.systemDefault());
    }

    private LocalDateTime currentDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(clock.wallTime()), ZoneId.systemDefault());
    }

    public record FareCalculationOutcome(FareResponse fare, boolean created) {}
}