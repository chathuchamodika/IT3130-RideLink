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

@Document(collection = "payments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    private String id;

    /** Idempotency: the unique index guarantees one payment document per ride. */
    @Indexed(unique = true, name = "ux_payments_ride_id")
    private String rideId;

    /** Plain string ids owned by other services - never @DBRef (brief §6.1). */
    private String passengerId;
    private String driverId;
    private String fareRecordId;

    private BigDecimal amount;       // stored as Decimal128 (see MongoConfig)
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String failureReason;
    private int attempts;

    private FareBreakdown fare;     // snapshot of the charged fare
    private Receipt receipt;        // embedded receipt -> single-document write, no transaction needed

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}