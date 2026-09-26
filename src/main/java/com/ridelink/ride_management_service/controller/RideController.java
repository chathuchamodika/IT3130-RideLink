package com.ridelink.ride_management_service.controller;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.model.RideStatus;
import com.ridelink.ride_management_service.service.RideService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // Create a new ride
    @PostMapping
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request) {

        RideResponse response = rideService.createRide(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all rides
    @GetMapping
    public ResponseEntity<List<RideResponse>> getAllRides() {

        return ResponseEntity.ok(rideService.getAllRides());
    }

    // Get a ride by ID
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRideById(
            @PathVariable String id) {

        return ResponseEntity.ok(rideService.getRideById(id));
    }

    // Update ride status
    @PatchMapping("/{id}/status")
    public ResponseEntity<RideResponse> updateRideStatus(
            @PathVariable String id,
            @RequestParam RideStatus status) {

        return ResponseEntity.ok(
                rideService.updateRideStatus(id, status)
        );
    }

    // Cancel a ride
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable String id) {

        return ResponseEntity.ok(
                rideService.cancelRide(id)
        );
    }
}
