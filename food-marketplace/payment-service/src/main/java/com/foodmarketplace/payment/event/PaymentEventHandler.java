package com.foodmarketplace.payment.event;

import com.foodmarketplace.payment.service.PaymentCompensationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class PaymentEventHandler {

    private static final Logger LOGGER = Logger.getLogger(PaymentEventHandler.class.getName());

    @Inject
    PaymentCompensationService compensationService;

    @Incoming("order-events")
    public CompletionStage<Void> handleOrderEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Payment Service received order event: " + eventData);
            
            // Handle payment-related order events
            if (eventData.contains("PAYMENT_RESERVATION_REQUESTED")) {
                handlePaymentReservationRequest(eventData);
            } else if (eventData.contains("PAYMENT_REFUND_REQUESTED")) {
                handlePaymentRefundRequest(eventData);
            } else if (eventData.contains("ORDER_SAGA_COMPLETED")) {
                handleOrderSagaCompleted(eventData);
            } else if (eventData.contains("INVENTORY_RELEASE_REQUESTED")) {
                // Handle inventory release (might trigger payment release)
                handleInventoryReleaseRequest(eventData);
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing order event in Payment Service: " + e.getMessage());
            return message.nack(e);
        }
    }

    private void handlePaymentReservationRequest(String eventData) {
        try {
            LOGGER.info("Processing payment reservation request");
            
            // Parse event data (simplified - use proper JSON parsing in production)
            String orderIdStr = extractValue(eventData, "orderId");
            String customerIdStr = extractValue(eventData, "customerId");
            String amountStr = extractValue(eventData, "amount");
            
            if (orderIdStr != null && customerIdStr != null && amountStr != null) {
                UUID orderId = UUID.fromString(orderIdStr);
                UUID customerId = UUID.fromString(customerIdStr);
                Double amount = Double.parseDouble(amountStr);
                
                compensationService.handlePaymentReservationRequest(orderId, customerId, amount);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling payment reservation request: " + e.getMessage());
        }
    }

    private void handlePaymentRefundRequest(String eventData) {
        try {
            LOGGER.info("Processing payment refund request");
            
            String orderIdStr = extractValue(eventData, "orderId");
            String paymentIdStr = extractValue(eventData, "paymentId");
            String reasonStr = extractValue(eventData, "reason");
            
            if (orderIdStr != null) {
                UUID orderId = UUID.fromString(orderIdStr);
                compensationService.handlePaymentRefundRequest(orderId, paymentIdStr, reasonStr);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling payment refund request: " + e.getMessage());
        }
    }

    private void handleOrderSagaCompleted(String eventData) {
        try {
            LOGGER.info("Processing order saga completion - capturing payment");
            
            String orderIdStr = extractValue(eventData, "orderId");
            
            if (orderIdStr != null) {
                UUID orderId = UUID.fromString(orderIdStr);
                compensationService.capturePayment(orderId);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling order saga completion: " + e.getMessage());
        }
    }

    private void handleInventoryReleaseRequest(String eventData) {
        try {
            LOGGER.info("Processing inventory release - checking payment compensation");
            
            String orderIdStr = extractValue(eventData, "orderId");
            String reasonStr = extractValue(eventData, "reason");
            
            if (orderIdStr != null) {
                UUID orderId = UUID.fromString(orderIdStr);
                
                // If inventory is being released, we might need to release payment reservation too
                if ("PAYMENT_FAILED".equals(reasonStr) || "SAGA_TIMEOUT".equals(reasonStr)) {
                    compensationService.releasePaymentReservation(orderId, reasonStr);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling inventory release request: " + e.getMessage());
        }
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            // Try without quotes for numbers/booleans
            searchKey = "\"" + key + "\":";
            startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            startIndex += searchKey.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            return json.substring(startIndex, endIndex).trim();
        } else {
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        }
    }
}
