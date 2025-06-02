package com.foodmarketplace.review.event;

import com.foodmarketplace.review.service.ReviewService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class ReviewEventHandler {

    private static final Logger LOGGER = Logger.getLogger(ReviewEventHandler.class.getName());

    @Inject
    ReviewService reviewService;

    @Incoming("order-events")
    public CompletionStage<Void> handleOrderEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Received order event: " + eventData);
            
            // Parse event and handle based on type
            if (eventData.contains("ORDER_DELIVERED")) {
                // Extract order info and mark as ready for review
                // This would enable customers to leave reviews
                LOGGER.info("Order delivered - ready for review");
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing order event: " + e.getMessage());
            return message.nack(e);
        }
    }

    @Incoming("user-events")  
    public CompletionStage<Void> handleUserEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Received user event: " + eventData);
            
            // Handle user-related events that might affect reviews
            if (eventData.contains("USER_STATUS_UPDATED")) {
                // Handle user status changes
                LOGGER.info("User status updated - checking review eligibility");
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing user event: " + e.getMessage());
            return message.nack(e);
        }
    }

    @Incoming("menu-events")
    public CompletionStage<Void> handleMenuEvent(Message<String> message) {
        try {
            String eventData = message.getPayload();
            LOGGER.info("Received menu event: " + eventData);
            
            // Handle menu/dish events
            if (eventData.contains("DISH_UPDATED")) {
                // Update dish-related review aggregations
                LOGGER.info("Dish updated - recalculating review stats");
            }
            
            return message.ack();
        } catch (Exception e) {
            LOGGER.severe("Error processing menu event: " + e.getMessage());
            return message.nack(e);
        }
    }
}
