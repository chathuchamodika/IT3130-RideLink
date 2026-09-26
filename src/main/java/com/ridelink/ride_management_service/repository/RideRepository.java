package com.ridelink.ride_management_service.repository;

import com.ridelink.ride_management_service.model.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RideRepository extends MongoRepository<Ride, String> {
}
