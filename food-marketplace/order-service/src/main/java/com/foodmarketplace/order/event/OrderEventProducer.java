package com.foodmarketplace.order.event;

import com.foodmarketplace.order.dto.OrderCreateDto;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class OrderEventProducer {

    @Inject
    @Channel("order-events")
    Emitter<String> eventEmitter;

    public void publishOrderCreationRequested(UUID orderId, OrderCreateDto orderDto) {
        String event = createEvent("ORDER_CREATION_REQUESTED", orderId, 
            String.format("{\"customerId\":\"%s\",\"deliveryType\":\"%s\",\"items\":%s}", 
                orderDto.customerId, orderDto.deliveryType, itemsToJson(orderDto.items)));
        eventEmitter.send(event);
    }

    public void publishOrderConfirmed(UUID orderId, UUID customerId, UUID cookId) {
        String event = createEvent("ORDER_CONFIRMED", orderId, 
            String.format("{\"customerId\":\"%s\",\"cookId\":\"%s\"}", customerId, cookId));
        eventEmitter.send(event);
    }

    public void publishOrderRejected(UUID orderId, UUID customerId, String reason) {
        String event = createEvent("ORDER_REJECTED", orderId, 
            String.format("{\"customerId\":\"%s\",\"reason\":\"%s\"}", customerId, reason));
        eventEmitter.send(event);
    }

    public void publishOrderStatusUpdated(UUID orderId, String oldStatus, String newStatus, 
                                        UUID customerId, UUID cookId) {
        String event = createEvent("ORDER_STATUS_UPDATED", orderId, 
            String.format("{\"oldStatus\":\"%s\",\"newStatus\":\"%s\",\"customerId\":\"%s\",\"cookId\":\"%s\"}", 
                oldStatus, newStatus, customerId, cookId));
        eventEmitter.send(event);
    }

    public void publishOrderCancelled(UUID orderId, UUID customerId, UUID cookId, String reason) {
        String event = createEvent("ORDER_CANCELLED", orderId, 
            String.format("{\"customerId\":\"%s\",\"cookId\":\"%s\",\"reason\":\"%s\"}", 
                customerId, cookId, reason));
        eventEmitter.send(event);
    }

    public void publishOrderReviewed(UUID orderId, UUID customerId, UUID cookId, 
                                   BigDecimal rating, String comment) {
        String event = createEvent("ORDER_REVIEWED", orderId, 
            String.format("{\"customerId\":\"%s\",\"cookId\":\"%s\",\"rating\":%.2f,\"comment\":\"%s\"}", 
                customerId, cookId, rating.doubleValue(), comment != null ? comment : ""));
        eventEmitter.send(event);
    }

    private String createEvent(String eventType, UUID orderId, String data) {
        return String.format(
            "{\"eventType\":\"%s\",\"orderId\":\"%s\",\"timestamp\":\"%s\",\"data\":%s}",
            eventType, orderId, LocalDateTime.now(), data
        );
    }

    private String itemsToJson(java.util.List<OrderCreateDto.OrderItemDto> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            OrderCreateDto.OrderItemDto item = items.get(i);
            sb.append(String.format("{\"dishId\":\"%s\",\"quantity\":%d}", 
                item.dishId, item.quantity));
        }
        sb.append("]");
        return sb.toString();
    }
}
