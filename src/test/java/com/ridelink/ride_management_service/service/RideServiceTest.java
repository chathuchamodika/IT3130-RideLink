package com.ridelink.ride_management_service.service;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.exception.RideNotFoundException;
import com.ridelink.ride_management_service.model.Ride;
import com.ridelink.ride_management_service.model.RideStatus;
import com.ridelink.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideService rideService;

    private Ride ride;

    @BeforeEach
    void setUp() {
        ride = new Ride(
                "passenger001",
                null,
                "Colombo",
                "Kandy",
                RideStatus.REQUESTED
        );

        ride.setId("ride001");
    }

    @Test
    void createRide_shouldCreateRideSuccessfully() {

        CreateRideRequest request = new CreateRideRequest();

        request.setPassengerId("passenger001");
        request.setPickupLocation("Colombo");
        request.setDestination("Kandy");

        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        RideResponse response = rideService.createRide(request);

        assertNotNull(response);
        assertEquals("ride001", response.getId());
        assertEquals("passenger001", response.getPassengerId());
        assertEquals("Colombo", response.getPickupLocation());
        assertEquals("Kandy", response.getDestination());
        assertEquals(RideStatus.REQUESTED, response.getStatus());

        verify(rideRepository, times(1))
                .save(any(Ride.class));
    }

    @Test
    void getAllRides_shouldReturnAllRides() {

        Ride secondRide = new Ride(
                "passenger002",
                null,
                "Galle",
                "Matara",
                RideStatus.REQUESTED
        );

        secondRide.setId("ride002");

        when(rideRepository.findAll())
                .thenReturn(List.of(ride, secondRide));

        List<RideResponse> responses =
                rideService.getAllRides();

        assertEquals(2, responses.size());

        assertEquals("ride001", responses.get(0).getId());
        assertEquals("ride002", responses.get(1).getId());

        verify(rideRepository, times(1))
                .findAll();
    }

    @Test
    void getRideById_shouldReturnRide() {

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        RideResponse response =
                rideService.getRideById("ride001");

        assertNotNull(response);
        assertEquals("ride001", response.getId());
        assertEquals("passenger001", response.getPassengerId());
        assertEquals("Colombo", response.getPickupLocation());
        assertEquals("Kandy", response.getDestination());
        assertEquals(RideStatus.REQUESTED, response.getStatus());

        verify(rideRepository, times(1))
                .findById("ride001");
    }

    @Test
    void getRideById_shouldThrowExceptionWhenRideNotFound() {

        when(rideRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideById("invalid-id")
        );

        verify(rideRepository, times(1))
                .findById("invalid-id");
    }

    @Test
    void updateRideStatus_shouldUpdateStatus() {

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        RideResponse response =
                rideService.updateRideStatus(
                        "ride001",
                        RideStatus.ACCEPTED
                );

        assertNotNull(response);
        assertEquals(RideStatus.ACCEPTED, response.getStatus());

        verify(rideRepository, times(1))
                .findById("ride001");

        verify(rideRepository, times(1))
                .save(ride);
    }

    @Test
    void cancelRide_shouldSetStatusToCancelled() {

        when(rideRepository.findById("ride001"))
                .thenReturn(Optional.of(ride));

        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        RideResponse response =
                rideService.cancelRide("ride001");

        assertNotNull(response);
        assertEquals(
                RideStatus.CANCELLED,
                response.getStatus()
        );

        verify(rideRepository, times(1))
                .findById("ride001");

        verify(rideRepository, times(1))
                .save(ride);
    }
}