package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ridelink.farepayment.domain.Receipt;
import com.ridelink.farepayment.domain.Payment;

public record ReceiptResponse(String receiptNumber, String paymentId, String rideId, String passengerId,
                              BigDecimal amountPaid, String currency, String method, LocalDateTime issuedAt) {

    public static ReceiptResponse from(Payment p) {
        Receipt r = p.getReceipt();
        return new ReceiptResponse(r.getReceiptNumber(), p.getId(), r.getRideId(), r.getPassengerId(),
                r.getAmountPaid(), r.getCurrency(), r.getMethod(), r.getIssuedAt());
    }
}