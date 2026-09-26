package com.ridelink.farepayment.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import com.mongodb.DuplicateKeyException;
import com.ridelink.farepayment.domain.FareRecord;
import com.ridelink.farepayment.domain.Payment;
import com.ridelink.farepayment.domain.PaymentStatus;
import com.ridelink.farepayment.domain.Receipt;
import com.ridelink.farepayment.dto.AuthenticatedUser;
import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.dto.response.PaymentResponse;
import com.ridelink.farepayment.dto.response.ReceiptResponse;
import com.ridelink.farepayment.exception.DuplicatePaymentException;
import com.ridelink.farepayment.exception.FareNotFoundException;
import com.ridelink.farepayment.exception.PaymentFailedException;
import com.ridelink.farepayment.exception.PaymentNotFoundException;
import com.ridelink.farepayment.exception.ReceiptNotFoundException;
import com.ridelink.farepayment.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareRecordRepository fareRecordRepository;
    private final PaymentGateway paymentGateway;

    /**
     * Records a simulated payment. Idempotency:
     *  - application check: an existing SUCCESSFUL payment -> 409
     *  - a FAILED attempt is retried by updating the same document (attempts++)
     *  - the unique index on rideId is the final guard against concurrent double payment
     */
    public PaymentOutcome recordPayment(PaymentRequest request, AuthenticatedUser requester) {
        FareRecord fare = fareRecordRepository.findByRideId(request.rideId())
            .orElseThrow(() -> new FareNotFoundException(request.rideId()));

        if (!requester.admin() && !requester.userId().equals(fare.getPassengerId())) {
            throw new AccessDeniedException("Only the passenger of the ride (or an admin) can pay for it");
        }

        Payment payment = paymentRepository.findByRideId(request.rideId()).orElse(null);
        boolean created = payment == null;

        if (created) {
            payment = newPayment(fare, request);
        } else {
            if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                throw new DuplicatePaymentException(request.rideId());
            }
            // Retry after a failed attempt.
            payment.setMethod(request.method());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setFailureReason(null);
            payment.setAttempts(payment.getAttempts() + 1);
            payment.setUpdatedAt(LocalDateTime.now());
        }

        GatewayResult result = paymentGateway.process(request.method(), payment.getAmount(),
                payment.getCurrency(), Boolean.TRUE.equals(request.simulateFailure()));

        payment.setStatus(result.successful() ? PaymentStatus.SUCCESSFUL : PaymentStatus.FAILED);
        payment.setFailureReason(result.failureReason());
        payment.setUpdatedAt(LocalDateTime.now());
        if (result.successful()) {
            payment.setReceipt(buildReceipt(payment));
        }

        Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (DuplicateKeyException e) {
            throw new DuplicatePaymentException(request.rideId());   // race-condition guard
        }
        if (!result.successful()) {
            throw new PaymentFailedException(saved.getId(), result.failureReason());
        }
        return new PaymentOutcome(PaymentResponse.from(saved), created);
    }

    public PaymentResponse getPayment(String paymentId, AuthenticatedUser requester) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with id " + paymentId));
        assertCanAccess(requester, payment);
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentForRide(String rideId, AuthenticatedUser requester) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for ride " + rideId));
        assertCanAccess(requester, payment);
        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> findPaymentsForPassenger(String passengerId) {
        return paymentRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> findAll(PaymentStatus status) {
        List<Payment> payments = (List<Payment>) (status == null
                ? paymentRepository.findAll() : paymentRepository.findByStatus(status));
        return payments.stream().map(PaymentResponse::from).toList();
    }

    public ReceiptResponse getReceiptByNumber(String receiptNumber, AuthenticatedUser requester) {
        Payment payment = paymentRepository.findByReceiptReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ReceiptNotFoundException(receiptNumber));
        return receiptOf(payment, requester);
    }

    public ReceiptResponse getReceiptForRide(String rideId, AuthenticatedUser requester) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for ride " + rideId));
        return receiptOf(payment, requester);
    }

    private ReceiptResponse receiptOf(Payment payment, AuthenticatedUser requester) {
        assertCanAccess(requester, payment);
        if (payment.getReceipt() == null) {
            throw new ReceiptNotFoundException("none yet for ride " + payment.getRideId());
        }
        return ReceiptResponse.from(payment);
    }

    private void assertCanAccess(AuthenticatedUser requester, Payment payment) {
        if (requester.admin()
                || requester.userId().equals(payment.getPassengerId())
                || requester.userId().equals(payment.getDriverId())) {
            return;
        }
        throw new AccessDeniedException(
                "Only the passenger, the driver of the ride or an admin can view this payment");
    }

    private Payment newPayment(FareRecord fare, PaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.builder()
                .rideId(fare.getRideId())
                .passengerId(fare.getPassengerId())
                .driverId(fare.getDriverId())
                .fareRecordId(fare.getId())
                .amount(fare.getBreakdown().getTotal())
                .currency(fare.getBreakdown().getCurrency())
                .method(request.method())
                .status(PaymentStatus.PENDING)
                .attempts(1)
                .fare(fare.getBreakdown())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Receipt buildReceipt(Payment payment) {
        return Receipt.builder()
                .receiptNumber("RCPT-" + UUID.randomUUID())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .amountPaid(payment.getAmount())
                .currency(payment.getCurrency())
                .method(payment.getMethod().toString())
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public record PaymentOutcome(PaymentResponse payment, boolean created) {}
}