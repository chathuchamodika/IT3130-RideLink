package com.ridelink.farepayment.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.client.RideNotCompletedException;
import com.ridelink.farepayment.client.RideSummary;
import com.ridelink.farepayment.domain.FareRecord;
import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.FareEstimateRequest;
import com.ridelink.farepayment.dto.request.FinalFareRequest;
import com.ridelink.farepayment.dto.request.LocationPoint;
import com.ridelink.farepayment.dto.response.FareEstimateResponse;
import com.ridelink.farepayment.exception.FareNotFoundException;
import com.ridelink.farepayment.properties.FareProperties;
import com.ridelink.farepayment.service.FareRecordRepository;
import com.ridelink.farepayment.service.RuleBasedFareCalculator;

@ExtendWith(MockitoExtension.class)
class FareServiceTest {

    @Mock private FareRecordRepository fareRecordRepository;
    @Mock private RideClient rideClient;

    @BeforeEach
    void setUp() {
        new FareServiceTest();
    }

    @Test
    void estimate_computesHaversineDistanceAndDuration() {
        FareEstimateRequest request = new FareEstimateRequest(
                new LocationPoint("Pickup", 7.0000, 80.0000),
                new LocationPoint("Destination", 7.1000, 80.0000)); // 0.1 deg latitude apart

        FareEstimateResponse response = FareServiceTest.estimate(request);

        assertEquals(new BigDecimal("11.12"), response.distanceKm());
        assertEquals(23, response.estimatedDurationMinutes());       // 11.12 km at 30 km/h -> ceil(22.24)
        assertEquals(new BigDecimal("1112.00"), response.fare().distanceCharge());
        assertEquals(new BigDecimal("1492.00"), response.fare().total());
        verifyNoInteractions(fareRecordRepository);                  // estimates are stateless
    }

    private static FareEstimateResponse estimate(FareEstimateRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'estimate'");
    }

    @Test
    void estimate_sameLocation_chargesMinimumFare() {
        FareEstimateRequest request = new FareEstimateRequest(
                new LocationPoint("A", 7.0, 80.0), new LocationPoint("A", 7.0, 80.0));

        FareEstimateResponse response = FareServiceTest.estimate(request);

        assertEquals(new BigDecimal("250.00"), response.fare().total());
    }

    @Test
    void calculateFinalFare_persistsFareRecord() {
        when(rideClient.fetchRide("R-1")).thenReturn(Optional.of(
                new RideSummary("R-1", "COMPLETED", "P-1", "D-1", new BigDecimal("5"), 10)));
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.empty());
        when(fareRecordRepository.save(any(FareRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FareServiceTest.FareCalculationOutcome outcome = FareServiceTest.calculateFinalFare(
                new FinalFareRequest("R-1", "P-1", "D-1", new BigDecimal("5"), 10,
                        LocalDateTime.of(2026, 9, 30, 10, 0)));

        assertTrue(outcome.created());
        assertEquals(new BigDecimal("750.00"), outcome.fare().fare().total());
        verify(fareRecordRepository).save(any(FareRecord.class));
    }

    @Test
    void calculateFinalFare_rideNotCompleted_throws() {
        when(rideClient.fetchRide("R-2")).thenReturn(Optional.of(
                new RideSummary("R-2", "IN_PROGRESS", "P-1", "D-1", new BigDecimal("5"), 10)));

        assertThrows(RideNotCompletedException.class, () -> FareServiceTest.calculateFinalFare(
                new FinalFareRequest("R-2", "P-1", "D-1", new BigDecimal("5"), 10, null)));
        verifyNoInteractions(fareRecordRepository);
    }

    private void assertThrows(Class<FareNotFoundException> class1, Executable executable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assertThrows'");
    }

    @Test
    void calculateFinalFare_alreadyCalculated_isIdempotent() {
        when(rideClient.fetchRide("R-3")).thenReturn(Optional.of(
                new RideSummary("R-3", "COMPLETED", "P-1", "D-1", new BigDecimal("5"), 10)));
        when(fareRecordRepository.findByRideId("R-3")).thenReturn(Optional.of(
                FareRecord.builder().rideId("R-3").passengerId("P-1").driverId("D-1")
                        .distanceKm(new BigDecimal("5")).durationMinutes(10).build()));

        FareServiceTest.FareCalculationOutcome outcome = FareServiceTest.calculateFinalFare(
                new FinalFareRequest("R-3", "P-1", "D-1", new BigDecimal("5"), 10, null));

        assertFalse(outcome.created());
        verify(fareRecordRepository, never()).save(any());
    }

    @Test
    void calculateFinalFare_integrationDisabled_skipsVerification() {
        when(rideClient.fetchRide(toString())).thenReturn(Optional.empty());
        when(fareRecordRepository.findByRideId("R-4")).thenReturn(Optional.empty());
        when(fareRecordRepository.save(any(FareRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FareServiceTest.FareCalculationOutcome outcome = FareServiceTest.calculateFinalFare(
                new FinalFareRequest("R-4", "P-1", "D-1", new BigDecimal("5"), 10, null));

        assertTrue(outcome.created());
    }

    private static FareCalculationOutcome calculateFinalFare(FinalFareRequest finalFareRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateFinalFare'");
    }

    @Test
    void getByRideId_unknownRide_throwsFareNotFound() {
        when(fareRecordRepository.findByRideId("unknown")).thenReturn(Optional.empty());
        assertThrows(FareNotFoundException.class,
                () -> FareServiceTest.getByRideId("unknown", new AuthenticatedUser("P-1", false)));
    }

    private static Object getByRideId(String string, AuthenticatedUser authenticatedUser) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByRideId'");
    }

    public class FareCalculationOutcome {

        public BooleanSupplier created() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'created'");
        }

        public FareEstimateResponse fare() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'fare'");
        }
    }
}