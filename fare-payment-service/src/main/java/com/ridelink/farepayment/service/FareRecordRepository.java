package com.ridelink.farepayment.service;

import com.ridelink.farepayment.domain.FareRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * FareRecordRepository
 */
public interface FareRecordRepository extends MongoRepository<FareRecord, String> {
    java.util.Optional<FareRecord> findByRideId(String rideId);
}
