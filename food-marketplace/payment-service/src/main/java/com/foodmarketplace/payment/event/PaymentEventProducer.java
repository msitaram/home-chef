package com.foodmarketplace.payment.event;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class PaymentEventProducer {

    @Inject
    @Channel("payment-events")
    Emitter<String> eventEmitter;

    // ========== SAGA SUCCESS EVENTS ==========

    public void publishPaymentReservationSuccess(UUID orderId, String paymentId) {
        String event = createEvent("PAYMENT_RESERVATION_SUCCESS", orderId, 
            String.format("{\"paymentId\":\"%s\"}", paymentId));
        eventEmitter.send(event);
    }

    public void publishPaymentCaptured(UUID orderId, Double amount) {
        String event = createEvent("PAYMENT_CAPTURED", orderId, 
            String.format("{\"amount\":%.2f}", amount));
        eventEmitter.send(event);
    }

    public void publishPaymentRefundSuccess(UUID orderId, Double amount) {
        String event = createEvent("PAYMENT_REFUND_SUCCESS", orderId, 
            String.format("{\"amount\":%.2f}", amount));
        eventEmitter.send(event);
    }

    public void publishPaymentReservationReleased(UUID orderId, String reason) {
        String event = createEvent("PAYMENT_RESERVATION_RELEASED", orderId, 
            String.format("{\"reason\":\"%s\"}", reason));
        eventEmitter.send(event);
    }

    // ========== SAGA FAILURE EVENTS ==========

    public void publishPaymentReservationFailure(UUID orderId, String reason) {
        String event = createEvent("PAYMENT_RESERVATION_FAILURE", orderId, 
            String.format("{\"reason\":\"%s\"}", reason));
        eventEmitter.send(event);
    }

    public void publishPaymentCaptureFailed(UUID orderId, String reason) {
        String event = createEvent("PAYMENT_CAPTURE_FAILED", orderId, 
            String.format("{\"reason\":\"%s\"}", reason));
        eventEmitter.send(event);
    }

    public void publishPaymentRefundFailure(UUID orderId, String reason) {
        String event = createEvent("PAYMENT_REFUND_FAILURE", orderId, 
            String.format("{\"reason\":\"%s\"}", reason));
        eventEmitter.send(event);
    }

    // ========== COMPENSATION EVENTS ==========

    public void publishCompensationRequired(UUID orderId, String compensationType, String reason) {
        String event = createEvent("COMPENSATION_REQUIRED", orderId, 
            String.format("{\"compensationType\":\"%s\",\"reason\":\"%s\"}", compensationType, reason));
        eventEmitter.send(event);
    }

    public void publishCompensationCompleted(UUID orderId, String compensationType) {
        String event = createEvent("COMPENSATION_COMPLETED", orderId, 
            String.format("{\"compensationType\":\"%s\"}", compensationType));
        eventEmitter.send(event);
    }

    public void publishCompensationFailed(UUID orderId, String compensationType, String reason) {
        String event = createEvent("COMPENSATION_FAILED", orderId, 
            String.format("{\"compensationType\":\"%s\",\"reason\":\"%s\"}", compensationType, reason));
        eventEmitter.send(event);
    }

    // ========== MONITORING EVENTS ==========

    public void publishPaymentMetrics(String metricType, Double value) {
        String event = createEvent("PAYMENT_METRICS", null, 
            String.format("{\"metricType\":\"%s\",\"value\":%.2f}", metricType, value));
        eventEmitter.send(event);
    }

    private String createEvent(String eventType, UUID orderId, String data) {
        return String.format(
            "{\"eventType\":\"%s\",\"orderId\":\"%s\",\"timestamp\":\"%s\",\"data\":%s}",
            eventType, orderId != null ? orderId.toString() : "null", LocalDateTime.now(), data
        );
    }
}
