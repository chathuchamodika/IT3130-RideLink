package com.ridelink.farepayment.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.DuplicateKeyException;
import com.ridelink.farepayment.domain.FareBreakdown;
import com.ridelink.farepayment.domain.FareRecord;
import com.ridelink.farepayment.domain.Payment;
import com.ridelink.farepayment.domain.PaymentMethod;
import com.ridelink.farepayment.domain.PaymentStatus;
import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.exception.DuplicatePaymentException;
import com.ridelink.farepayment.exception.FareNotFoundException;
import com.ridelink.farepayment.exception.PaymentFailedException;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.service.FareRecordRepository;
import com.ridelink.farepayment.service.GatewayResult;
import com.ridelink.farepayment.service.PaymentGateway;
import com.ridelink.farepayment.service.PaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private FareRecordRepository fareRecordRepository;
    @Mock private PaymentGateway paymentGateway;

    private PaymentService paymentService;

    private final AuthenticatedUser passenger = new AuthenticatedUser("P-1", false);

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, fareRecordRepository, paymentGateway);
    }

    private FareRecord fareFor(String rideId, String passengerId) {
        return FareRecord.builder().id("fare-" + rideId).rideId(rideId)
                .passengerId(passengerId).driverId("D-1")
                .distanceKm(new BigDecimal("5")).durationMinutes(10)
                .breakdown(FareBreakdown.builder().currency("LKR")
                        .baseFare(new BigDecimal("150.00")).distanceKm(new BigDecimal("5"))
                        .distanceCharge(new BigDecimal("500.00")).durationMinutes(10)
                        .timeCharge(new BigDecimal("100.00"))
                        .nightSurcharge(BigDecimal.ZERO).total(new BigDecimal("750.00")).build())
                .calculatedAt(LocalDateTime.now()).build();
    }

    private Payment existingPayment(String rideId, PaymentStatus status) {
        return Payment.builder().id("pay-" + rideId).rideId(rideId)
                .passengerId("P-1").driverId("D-1")
                .amount(new BigDecimal("750.00")).currency("LKR").method(PaymentMethod.CARD)
                .status(status).attempts(1).fare(fareFor(rideId, "P-1").getBreakdown())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    @Test
    void recordPayment_success_persistsPaymentWithReceipt() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-1")));
        when(paymentRepository.findByRideId("R-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.process(any(), any(), any(), anyBoolean()))
                .thenReturn(GatewayResult.success());

        PaymentService.PaymentOutcome outcome = paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.CARD, null), passenger);

        assertTrue(outcome.created());
        assertEquals(PaymentStatus.SUCCESSFUL, outcome.payment().status());
        assertEquals(new BigDecimal("750.00"), outcome.payment().amount());
        assertNotNull(outcome.payment().receipt());
        assertNotNull(outcome.payment().receipt().receiptNumber());
    }

    @Test
    void recordPayment_rideAlreadyPaid_throwsDuplicate() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-1")));
        when(paymentRepository.findByRideId("R-1"))
                .thenReturn(Optional.of(existingPayment("R-1", PaymentStatus.SUCCESSFUL)));

        assertThrows(DuplicatePaymentException.class, () -> paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.CARD, null), passenger));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void recordPayment_gatewayDeclines_persistsFailedPaymentAndThrows() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-1")));
        when(paymentRepository.findByRideId("R-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.process(any(), any(), any(), anyBoolean()))
                .thenReturn(GatewayResult.failure("Simulated decline"));

        assertThrows(PaymentFailedException.class, () -> paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.CARD, true), passenger));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertEquals("Simulated decline", captor.getValue().getFailureReason());
        assertNull(captor.getValue().getReceipt());
    }

    @Test
    void recordPayment_retryAfterFailure_updatesSameDocument() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-1")));
        when(paymentRepository.findByRideId("R-1"))
                .thenReturn(Optional.of(existingPayment("R-1", PaymentStatus.FAILED)));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.process(any(), any(), any(), anyBoolean()))
                .thenReturn(GatewayResult.success());

        PaymentService.PaymentOutcome outcome = paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.WALLET, null), passenger);

        assertFalse(outcome.created());                              // same document updated
        assertEquals(PaymentStatus.SUCCESSFUL, outcome.payment().status());
        assertEquals(2, outcome.payment().attempts());
        assertEquals(PaymentMethod.WALLET, outcome.payment().method());
        assertNotNull(outcome.payment().receipt());
    }

    @Test
    void recordPayment_fareNotCalculated_throwsFareNotFound() {
        when(fareRecordRepository.findByRideId("R-9")).thenReturn(Optional.empty());

        assertThrows(FareNotFoundException.class, () -> paymentService.recordPayment(
                new PaymentRequest("R-9", PaymentMethod.CARD, null), passenger));
        verifyNoInteractions(paymentRepository, paymentGateway);
    }

    @Test
    void recordPayment_otherPassenger_throwsAccessDenied() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-2")));

        assertThrows(AccessDeniedException.class, () -> paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.CARD, null), passenger));
        verifyNoInteractions(paymentRepository, paymentGateway);
    }

    @Test
    void recordPayment_uniqueIndexViolation_mappedToDuplicatePayment() {
        when(fareRecordRepository.findByRideId("R-1")).thenReturn(Optional.of(fareFor("R-1", "P-1")));
        when(paymentRepository.findByRideId("R-1")).thenReturn(Optional.empty());
        when(paymentGateway.process(any(), any(), any(), anyBoolean()))
                .thenReturn(GatewayResult.success());
        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(org.mockito.Mockito.mock(DuplicateKeyException.class));

        assertThrows(DuplicatePaymentException.class, () -> paymentService.recordPayment(
                new PaymentRequest("R-1", PaymentMethod.CARD, null), passenger));
    }
}