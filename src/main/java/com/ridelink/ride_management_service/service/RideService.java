package com.ridelink.ride_management_service.service;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.model.Ride;
import com.ridelink.ride_management_service.model.RideStatus;
import com.ridelink.ride_management_service.repository.RideRepository;

import com.ridelink.ride_management_service.exception.RideNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    // Create a new ride
    public RideResponse createRide(CreateRideRequest request) {

        Ride ride = new Ride();

        ride.setPassengerId(request.getPassengerId());
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDestination(request.getDestination());

        // Driver is not assigned when ride is first created
        ride.setDriverId(null);

        // New rides start with REQUESTED status
        ride.setStatus(RideStatus.REQUESTED);

        Ride savedRide = rideRepository.save(ride);

        return convertToResponse(savedRide);
    }

    // Get all rides
    public List<RideResponse> getAllRides() {

        return rideRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get one ride by ID
    public RideResponse getRideById(String id) {

        Ride ride = rideRepository.findById(id)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride not found with id: " + id)
                );

        return convertToResponse(ride);
    }

    // Update ride status
    public RideResponse updateRideStatus(String id, RideStatus status) {

        Ride ride = rideRepository.findById(id)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride not found with id: " + id)
                );

        ride.setStatus(status);

        Ride updatedRide = rideRepository.save(ride);

        return convertToResponse(updatedRide);
    }

    // Cancel a ride
    public RideResponse cancelRide(String id) {

        Ride ride = rideRepository.findById(id)
                .orElseThrow(() ->
                        new RideNotFoundException("Ride not found with id: " + id)
                );

        ride.setStatus(RideStatus.CANCELLED);

        Ride updatedRide = rideRepository.save(ride);

        return convertToResponse(updatedRide);
    }

    // Convert Ride entity to RideResponse DTO
    private RideResponse convertToResponse(Ride ride) {

        return new RideResponse(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getPickupLocation(),
                ride.getDestination(),
                ride.getStatus()
        );
    }
}
