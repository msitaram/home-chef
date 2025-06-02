package com.foodmarketplace.order.saga;

import com.foodmarketplace.order.entity.Order;
import com.foodmarketplace.order.entity.Order.OrderStatus;
import com.foodmarketplace.order.event.OrderEventProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * SAGA Pattern Implementation for Order Processing
 * Handles compensation and rollback scenarios
 */
@ApplicationScoped
public class OrderSagaOrchestrator {

    private static final Logger LOGGER = Logger.getLogger(OrderSagaOrchestrator.class.getName());

    @Inject
    OrderEventProducer eventProducer;

    /**
     * Start Order Processing Saga
     * Step 1: Validate inventory
     * Step 2: Reserve payment
     * Step 3: Confirm order
     */
    @Transactional
    public void startOrderSaga(UUID orderId) {
        LOGGER.info("Starting Order Saga for order: " + orderId);
        
        Order order = Order.findById(orderId);
        if (order == null) {
            LOGGER.severe("Order not found for saga: " + orderId);
            return;
        }

        // Mark as saga in progress
        order.status = OrderStatus.PENDING;
        order.persist();

        // Step 1: Request inventory validation
        eventProducer.publishInventoryValidationRequested(orderId, order.customerId);
    }

    /**
     * Handle successful inventory validation
     * Proceed to payment reservation
     */
    @Transactional
    public void handleInventoryValidationSuccess(UUID orderId) {
        LOGGER.info("Inventory validation successful for order: " + orderId);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        // Step 2: Request payment reservation
        eventProducer.publishPaymentReservationRequested(orderId, order.customerId, order.totalAmount);
    }

    /**
     * Handle inventory validation failure
     * Compensate: Cancel order immediately
     */
    @Transactional
    public void handleInventoryValidationFailure(UUID orderId, String reason) {
        LOGGER.warning("Inventory validation failed for order: " + orderId + ", reason: " + reason);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        // Compensate: Cancel order
        order.status = OrderStatus.REJECTED;
        order.cancellationReason = "Inventory validation failed: " + reason;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

        // Publish compensation event
        eventProducer.publishOrderCompensationCompleted(orderId, "INVENTORY_VALIDATION_FAILED", reason);
    }

    /**
     * Handle successful payment reservation
     * Proceed to order confirmation
     */
    @Transactional
    public void handlePaymentReservationSuccess(UUID orderId, String paymentId) {
        LOGGER.info("Payment reservation successful for order: " + orderId + ", paymentId: " + paymentId);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        order.paymentTransactionId = paymentId;
        order.status = OrderStatus.CONFIRMED;
        order.confirmedAt = LocalDateTime.now();
        order.persist();

        // Step 3: Confirm order completion
        eventProducer.publishOrderSagaCompleted(orderId);
    }

    /**
     * Handle payment reservation failure
     * Compensate: Release inventory reservation
     */
    @Transactional
    public void handlePaymentReservationFailure(UUID orderId, String reason) {
        LOGGER.warning("Payment reservation failed for order: " + orderId + ", reason: " + reason);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        // Compensate: Release inventory and cancel order
        order.status = OrderStatus.REJECTED;
        order.cancellationReason = "Payment failed: " + reason;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

        // Step 1 Compensation: Release inventory reservation
        eventProducer.publishInventoryReleaseRequested(orderId, "PAYMENT_FAILED");
        
        // Publish compensation completion
        eventProducer.publishOrderCompensationCompleted(orderId, "PAYMENT_RESERVATION_FAILED", reason);
    }

    /**
     * Handle order cancellation after confirmation
     * Full compensation required
     */
    @Transactional
    public void handleOrderCancellationCompensation(UUID orderId, String reason) {
        LOGGER.info("Handling order cancellation compensation for: " + orderId);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        // Only allow cancellation for confirmed orders
        if (order.status != OrderStatus.CONFIRMED && order.status != OrderStatus.PREPARING) {
            LOGGER.warning("Cannot cancel order in status: " + order.status);
            return;
        }

        order.status = OrderStatus.CANCELLED;
        order.cancellationReason = reason;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

        // Full compensation workflow
        // Step 1: Refund payment
        if (order.paymentTransactionId != null) {
            eventProducer.publishPaymentRefundRequested(orderId, order.paymentTransactionId, reason);
        }

        // Step 2: Release inventory
        eventProducer.publishInventoryReleaseRequested(orderId, "ORDER_CANCELLED");

        // Step 3: Notify customer
        eventProducer.publishOrderCancellationNotificationRequested(orderId, order.customerId, reason);
    }

    /**
     * Handle timeout scenarios
     * Compensate: Cancel order and release resources
     */
    @Transactional
    public void handleSagaTimeout(UUID orderId, String step) {
        LOGGER.severe("Saga timeout for order: " + orderId + " at step: " + step);
        
        Order order = Order.findById(orderId);
        if (order == null) return;

        order.status = OrderStatus.REJECTED;
        order.cancellationReason = "Saga timeout at step: " + step;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

        // Full compensation for timeout
        switch (step) {
            case "PAYMENT_RESERVATION":
                // Release inventory if payment times out
                eventProducer.publishInventoryReleaseRequested(orderId, "SAGA_TIMEOUT");
                break;
            case "ORDER_CONFIRMATION":
                // Refund payment if confirmation times out
                if (order.paymentTransactionId != null) {
                    eventProducer.publishPaymentRefundRequested(orderId, order.paymentTransactionId, "SAGA_TIMEOUT");
                }
                eventProducer.publishInventoryReleaseRequested(orderId, "SAGA_TIMEOUT");
                break;
        }

        eventProducer.publishOrderCompensationCompleted(orderId, "SAGA_TIMEOUT", step);
    }
}
