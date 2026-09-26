package com.ridelink.farepayment.controller;

import java.util.List;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.domain.PaymentStatus;
import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.dto.response.PaymentResponse;
import com.ridelink.farepayment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Simulated payment recording, status and history")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Record a simulated payment for a ride",
            description = "The final fare must exist first. One successful payment per ride; "
                    + "a failed simulated payment is stored with status FAILED and is retried "
                    + "by repeating this request with the same rideId.")
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request,
                                               Authentication authentication) {
        PaymentService.PaymentOutcome outcome =
                paymentService.recordPayment(request, AuthenticatedUser.from(authentication));
        return ResponseEntity.status(outcome.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(outcome.payment());
    }

    @Operation(summary = "Get a payment by id")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId,
                                                      Authentication authentication) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId,
                AuthenticatedUser.from(authentication)));
    }

    @Operation(summary = "Get the payment for a ride")
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentForRide(@PathVariable String rideId,
                                                             Authentication authentication) {
        return ResponseEntity.ok(paymentService.getPaymentForRide(rideId,
                AuthenticatedUser.from(authentication)));
    }

    @Operation(summary = "List the current passenger's payments")
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> myPayments(Authentication authentication) {
        return ResponseEntity.ok(paymentService.findPaymentsForPassenger(authentication.name()));
    }

    @Operation(summary = "List all payments (admin only)")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> allPayments(
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(paymentService.findAll(status));
    }
}