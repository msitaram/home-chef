package com.foodmarketplace.order.event;

import com.foodmarketplace.order.service.EventDrivenOrderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class OrderEventHandler {

    private static final Logger LOGGER = Logger.getLogger(OrderEventHandler.class.getName());

    @Inject
    EventDrivenOrderService orderService;

    @Incoming("user-events")
    public CompletionStage<Void> handleUserEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Order Service received user event: " + eventData);
            
            // Handle user-related events that might affect orders
            if (eventData.contains("USER_STATUS_UPDATED")) {
                // Handle user status changes that might affect active orders
                LOGGER.info("User status updated - checking order implications");
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing user event in Order Service: " + e.getMessage());
            return message.nack(e);
        }
    }

    @Incoming("menu-events")
    public CompletionStage<Void> handleMenuEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Order Service received menu event: " + eventData);
            
            // Handle menu/dish events for order validation
            if (eventData.contains("DISH_AVAILABILITY_CHANGED")) {
                // Handle dish availability changes
                LOGGER.info("Dish availability changed - checking pending orders");
            } else if (eventData.contains("ORDER_VALIDATION_RESPONSE")) {
                // Handle validation response from Menu Service
                handleOrderValidationResponse(eventData);
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing menu event in Order Service: " + e.getMessage());
            return message.nack(e);
        }
    }

    private void handleOrderValidationResponse(String eventData) {
        try {
            // Parse validation response
            // Example format: {"eventType":"ORDER_VALIDATION_RESPONSE","orderId":"uuid","isValid":true,"totalAmount":250.00,"cookId":"uuid"}
            
            // This is a simplified parser - in production use proper JSON parsing
            if (eventData.contains("\"isValid\":true")) {
                LOGGER.info("Order validation successful - confirming order");
                
                // Extract order details (simplified - use proper JSON parsing in production)
                String orderIdStr = extractValue(eventData, "orderId");
                String totalAmountStr = extractValue(eventData, "totalAmount");
                String cookIdStr = extractValue(eventData, "cookId");
                
                if (orderIdStr != null && totalAmountStr != null && cookIdStr != null) {
                    UUID orderId = UUID.fromString(orderIdStr);
                    BigDecimal totalAmount = new BigDecimal(totalAmountStr);
                    UUID cookId = UUID.fromString(cookIdStr);
                    
                    orderService.processOrderValidation(orderId, true, null, totalAmount, cookId);
                }
            } else if (eventData.contains("\"isValid\":false")) {
                LOGGER.info("Order validation failed - rejecting order");
                
                String orderIdStr = extractValue(eventData, "orderId");
                String reasonStr = extractValue(eventData, "reason");
                
                if (orderIdStr != null) {
                    UUID orderId = UUID.fromString(orderIdStr);
                    orderService.processOrderValidation(orderId, false, reasonStr, null, null);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling order validation response: " + e.getMessage());
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
