package com.ridelink.farepayment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Receipt {
    private String receiptNumber;   // "RCPT-" + UUID
    private String rideId;
    private String passengerId;
    private BigDecimal amountPaid;
    private String currency;
    private String method;
    private LocalDateTime issuedAt;
}