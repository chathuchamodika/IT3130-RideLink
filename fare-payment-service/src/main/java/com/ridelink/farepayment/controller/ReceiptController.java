package com.ridelink.farepayment.controller;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.response.ReceiptResponse;
import com.ridelink.farepayment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Receipt retrieval")
public class ReceiptController {

    private final PaymentService paymentService;

    @Operation(summary = "Get a receipt by its receipt number")
    @GetMapping("/{receiptNumber}")
    public ResponseEntity<ReceiptResponse> getByReceiptNumber(@PathVariable String receiptNumber,
                                                              Authentication authentication) {
        return ResponseEntity.ok(paymentService.getReceiptByNumber(receiptNumber,
                AuthenticatedUser.from(authentication)));
    }

    @Operation(summary = "Get the receipt for a ride")
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<ReceiptResponse> getByRideId(@PathVariable String rideId,
                                                       Authentication authentication) {
        return ResponseEntity.ok(paymentService.getReceiptForRide(rideId,
                AuthenticatedUser.from(authentication)));
    }
}