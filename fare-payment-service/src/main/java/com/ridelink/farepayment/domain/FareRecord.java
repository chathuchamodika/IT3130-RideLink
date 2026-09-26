package com.ridelink.farepayment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "fare_records")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FareRecord {

    @Id
    private String id;

    /** Final fare is calculated once per ride (idempotent). */
    @Indexed(unique = true, name = "ux_fare_records_ride_id")
    private String rideId;

    private String passengerId;
    private String driverId;
    private BigDecimal distanceKm;
    private Integer durationMinutes;
    private FareBreakdown breakdown;
    private LocalDateTime calculatedAt;
}