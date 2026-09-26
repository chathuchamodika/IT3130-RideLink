package com.ridelink.ride_management_service.dto;

import com.ridelink.ride_management_service.model.RideStatus;

public class RideResponse {

    private String id;
    private String passengerId;
    private String driverId;
    private String pickupLocation;
    private String destination;
    private RideStatus status;

    public RideResponse() {
    }

    public RideResponse(String id,
                        String passengerId,
                        String driverId,
                        String pickupLocation,
                        String destination,
                        RideStatus status) {

        this.id = id;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}