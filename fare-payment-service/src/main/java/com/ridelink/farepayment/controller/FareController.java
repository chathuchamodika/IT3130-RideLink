package com.ridelink.farepayment.controller;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.FareEstimateRequest;
import com.ridelink.farepayment.dto.request.FinalFareRequest;
import com.ridelink.farepayment.dto.response.FareEstimateResponse;
import com.ridelink.farepayment.dto.response.FareResponse;
import com.ridelink.farepayment.service.FareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fares")
@RequiredArgsConstructor
@Tag(name = "Fares", description = "Fare estimation and final fare calculation")
public class FareController {

    private final FareService fareService;

    @Operation(summary = "Estimate a fare between pickup and destination (documented rule)")
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimate(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(fareService.estimate(request));
    }

    @Operation(summary = "Calculate and store the final fare for a completed ride",
            description = "Idempotent per rideId. If Ride Service integration is enabled, "
                    + "the ride must have status COMPLETED.")
    @PostMapping("/final")
    public ResponseEntity<FareResponse> calculateFinalFare(@Valid @RequestBody FinalFareRequest request) {
        FareService.FareCalculationOutcome outcome = fareService.calculateFinalFare(request);
        return ResponseEntity.status(outcome.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(outcome.fare());
    }

    @Operation(summary = "Get the stored final fare for a ride")
    @GetMapping("/{rideId}")
    public ResponseEntity<FareResponse> getFare(@PathVariable String rideId, Authentication authentication) {
        return ResponseEntity.ok(fareService.getByRideId(rideId, AuthenticatedUser.from(authentication)));
    }
}