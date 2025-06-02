package com.foodmarketplace.payment.service;

import com.foodmarketplace.payment.entity.PaymentTransaction;
import com.foodmarketplace.payment.entity.PaymentTransaction.*;
import com.foodmarketplace.payment.event.PaymentEventProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Payment Compensation Service - Handles SAGA compensation patterns
 * Ensures payment rollbacks and refunds work properly
 */
@ApplicationScoped
public class PaymentCompensationService {

    private static final Logger LOGGER = Logger.getLogger(PaymentCompensationService.class.getName());

    @Inject
    PaymentEventProducer eventProducer;

    @Inject
    PaymentGatewayService gatewayService;

    /**
     * Handle payment reservation request from Order SAGA
     */
    @Transactional
    public void handlePaymentReservationRequest(UUID orderId, UUID customerId, Double amount) {
        LOGGER.info("Processing payment reservation for order: " + orderId + ", amount: " + amount);

        try {
            // Create payment transaction
            PaymentTransaction payment = new PaymentTransaction();
            payment.orderId = orderId;
            payment.customerId = customerId;
            payment.amount = java.math.BigDecimal.valueOf(amount);
            payment.paymentMethod = PaymentMethod.UPI; // Default for demo
            payment.status = PaymentStatus.PENDING;
            payment.persist();

            // Simulate payment gateway reservation
            boolean reservationSuccess = gatewayService.reservePayment(payment);

            if (reservationSuccess) {
                payment.status = PaymentStatus.RESERVED;
                payment.reservedAt = LocalDateTime.now();
                payment.gatewayTransactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8);
                payment.persist();

                // Publish success event
                eventProducer.publishPaymentReservationSuccess(orderId, payment.id.toString());
                LOGGER.info("Payment reservation successful for order: " + orderId);

            } else {
                payment.status = PaymentStatus.FAILED;
                payment.failedAt = LocalDateTime.now();
                payment.failureReason = "Payment gateway reservation failed";
                payment.persist();

                // Publish failure event
                eventProducer.publishPaymentReservationFailure(orderId, "Payment gateway reservation failed");
                LOGGER.warning("Payment reservation failed for order: " + orderId);
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing payment reservation for order: " + orderId + ", error: " + e.getMessage());
            eventProducer.publishPaymentReservationFailure(orderId, "System error: " + e.getMessage());
        }
    }

    /**
     * Handle payment refund request (Compensation)
     */
    @Transactional
    public void handlePaymentRefundRequest(UUID orderId, String paymentId, String reason) {
        LOGGER.info("Processing payment refund for order: " + orderId + ", reason: " + reason);

        try {
            PaymentTransaction payment = PaymentTransaction.findByOrderId(orderId);
            if (payment == null) {
                LOGGER.warning("Payment not found for order: " + orderId);
                eventProducer.publishPaymentRefundFailure(orderId, "Payment not found");
                return;
            }

            // Only refund captured or reserved payments
            if (payment.status != PaymentStatus.CAPTURED && payment.status != PaymentStatus.RESERVED) {
                LOGGER.warning("Cannot refund payment in status: " + payment.status + " for order: " + orderId);
                eventProducer.publishPaymentRefundFailure(orderId, "Invalid payment status for refund");
                return;
            }

            // Mark compensation as pending
            payment.compensationStatus = CompensationStatus.PENDING;
            payment.compensationReason = reason;
            payment.compensationAttempts += 1;
            payment.persist();

            // Attempt refund through gateway
            boolean refundSuccess = gatewayService.refundPayment(payment);

            if (refundSuccess) {
                payment.status = PaymentStatus.REFUNDED;
                payment.compensationStatus = CompensationStatus.COMPLETED;
                payment.refundedAt = LocalDateTime.now();
                payment.persist();

                // Publish success event
                eventProducer.publishPaymentRefundSuccess(orderId, payment.amount.doubleValue());
                LOGGER.info("Payment refund successful for order: " + orderId);

            } else {
                payment.compensationStatus = CompensationStatus.FAILED;
                payment.persist();

                // Publish failure event
                eventProducer.publishPaymentRefundFailure(orderId, "Gateway refund failed");
                LOGGER.warning("Payment refund failed for order: " + orderId);

                // Schedule retry if attempts < max
                if (payment.compensationAttempts < 3) {
                    scheduleRefundRetry(payment.id);
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing payment refund for order: " + orderId + ", error: " + e.getMessage());
            eventProducer.publishPaymentRefundFailure(orderId, "System error: " + e.getMessage());
        }
    }

    /**
     * Capture payment after order confirmation
     */
    @Transactional
    public void capturePayment(UUID orderId) {
        LOGGER.info("Capturing payment for order: " + orderId);

        PaymentTransaction payment = PaymentTransaction.findByOrderId(orderId);
        if (payment == null || payment.status != PaymentStatus.RESERVED) {
            LOGGER.warning("Cannot capture payment for order: " + orderId + ", invalid state");
            return;
        }

        try {
            boolean captureSuccess = gatewayService.capturePayment(payment);

            if (captureSuccess) {
                payment.status = PaymentStatus.CAPTURED;
                payment.capturedAt = LocalDateTime.now();
                payment.persist();

                eventProducer.publishPaymentCaptured(orderId, payment.amount.doubleValue());
                LOGGER.info("Payment captured successfully for order: " + orderId);

            } else {
                payment.status = PaymentStatus.FAILED;
                payment.failureReason = "Payment capture failed";
                payment.failedAt = LocalDateTime.now();
                payment.persist();

                eventProducer.publishPaymentCaptureFailed(orderId, "Payment capture failed");
                LOGGER.warning("Payment capture failed for order: " + orderId);
            }

        } catch (Exception e) {
            LOGGER.severe("Error capturing payment for order: " + orderId + ", error: " + e.getMessage());
            eventProducer.publishPaymentCaptureFailed(orderId, "System error: " + e.getMessage());
        }
    }

    /**
     * Release payment reservation (Compensation)
     */
    @Transactional
    public void releasePaymentReservation(UUID orderId, String reason) {
        LOGGER.info("Releasing payment reservation for order: " + orderId + ", reason: " + reason);

        PaymentTransaction payment = PaymentTransaction.findByOrderId(orderId);
        if (payment == null || payment.status != PaymentStatus.RESERVED) {
            LOGGER.info("No reservation to release for order: " + orderId);
            return;
        }

        try {
            boolean releaseSuccess = gatewayService.releaseReservation(payment);

            if (releaseSuccess) {
                payment.status = PaymentStatus.FAILED;
                payment.failureReason = "Reservation released: " + reason;
                payment.failedAt = LocalDateTime.now();
                payment.compensationStatus = CompensationStatus.COMPLETED;
                payment.persist();

                eventProducer.publishPaymentReservationReleased(orderId, reason);
                LOGGER.info("Payment reservation released for order: " + orderId);

            } else {
                payment.compensationStatus = CompensationStatus.FAILED;
                payment.persist();
                LOGGER.warning("Failed to release payment reservation for order: " + orderId);
            }

        } catch (Exception e) {
            LOGGER.severe("Error releasing payment reservation for order: " + orderId + ", error: " + e.getMessage());
        }
    }

    /**
     * Schedule retry for failed compensations
     */
    private void scheduleRefundRetry(UUID paymentId) {
        // In production, this would use a scheduler/delayed message
        LOGGER.info("Scheduling refund retry for payment: " + paymentId);
        // Implementation would schedule a delayed retry
    }

    /**
     * Get payment statistics
     */
    public PaymentStats getPaymentStats() {
        long totalTransactions = PaymentTransaction.count();
        long successfulPayments = PaymentTransaction.count("status", PaymentStatus.CAPTURED);
        long failedPayments = PaymentTransaction.count("status", PaymentStatus.FAILED);
        long refundedPayments = PaymentTransaction.count("status", PaymentStatus.REFUNDED);
        long pendingCompensations = PaymentTransaction.count("compensationStatus", CompensationStatus.PENDING);

        return new PaymentStats(totalTransactions, successfulPayments, failedPayments, 
                              refundedPayments, pendingCompensations);
    }

    public static class PaymentStats {
        public long totalTransactions;
        public long successfulPayments;
        public long failedPayments;
        public long refundedPayments;
        public long pendingCompensations;

        public PaymentStats(long totalTransactions, long successfulPayments, long failedPayments,
                          long refundedPayments, long pendingCompensations) {
            this.totalTransactions = totalTransactions;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.refundedPayments = refundedPayments;
            this.pendingCompensations = pendingCompensations;
        }
    }
}
