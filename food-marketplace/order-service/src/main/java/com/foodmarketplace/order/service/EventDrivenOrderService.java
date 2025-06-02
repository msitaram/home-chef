package com.foodmarketplace.order.service;

import com.foodmarketplace.order.dto.OrderCreateDto;
import com.foodmarketplace.order.dto.OrderResponseDto;
import com.foodmarketplace.order.entity.Order;
import com.foodmarketplace.order.entity.Order.*;
import com.foodmarketplace.order.entity.OrderItem;
import com.foodmarketplace.order.event.OrderEventProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EventDrivenOrderService {

    @Inject
    OrderEventProducer eventProducer;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto orderDto) {
        // Validate basic order data
        if (orderDto.deliveryType == DeliveryType.DELIVERY) {
            if (orderDto.deliveryAddress == null || orderDto.deliveryAddress.trim().isEmpty()) {
                throw new BadRequestException("Delivery address is required for delivery orders");
            }
        }

        // Create order with PENDING status
        Order order = new Order();
        order.customerId = orderDto.customerId;
        order.deliveryType = orderDto.deliveryType;
        order.deliveryAddress = orderDto.deliveryAddress;
        order.deliveryCity = orderDto.deliveryCity;
        order.deliveryPincode = orderDto.deliveryPincode;
        order.deliveryInstructions = orderDto.deliveryInstructions;
        order.specialInstructions = orderDto.specialInstructions;
        order.paymentMethod = orderDto.paymentMethod;
        order.status = OrderStatus.PENDING;
        order.paymentStatus = PaymentStatus.PENDING;

        // Create order items (without validation - will be validated asynchronously)
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateDto.OrderItemDto itemDto : orderDto.items) {
            OrderItem orderItem = new OrderItem();
            orderItem.order = order;
            orderItem.dishId = itemDto.dishId;
            orderItem.dishName = "TBD"; // Will be filled by event
            orderItem.unitPrice = BigDecimal.ZERO; // Will be filled by event  
            orderItem.quantity = itemDto.quantity;
            orderItem.specialInstructions = itemDto.specialInstructions;
            orderItem.totalPrice = BigDecimal.ZERO; // Will be calculated by event

            orderItems.add(orderItem);
        }

        order.orderItems = orderItems;
        order.totalAmount = BigDecimal.ZERO; // Will be calculated asynchronously
        order.estimatedDeliveryTime = calculateEstimatedDeliveryTime(order.deliveryType);

        order.persist();

        // 🔥 PUBLISH ORDER_CREATION_REQUESTED EVENT (Async validation)
        eventProducer.publishOrderCreationRequested(order.id, orderDto);

        return OrderResponseDto.from(order);
    }

    @Transactional
    public void processOrderValidation(UUID orderId, boolean isValid, String validationMessage, 
                                     BigDecimal totalAmount, UUID cookId) {
        Order order = Order.findById(orderId);
        if (order == null) {
            return; // Order not found, ignore
        }

        if (isValid) {
            order.status = OrderStatus.CONFIRMED;
            order.totalAmount = totalAmount;
            order.cookId = cookId;
            order.confirmedAt = LocalDateTime.now();
            
            // 🔥 PUBLISH ORDER_CONFIRMED EVENT
            eventProducer.publishOrderConfirmed(order.id, order.customerId, order.cookId);
        } else {
            order.status = OrderStatus.REJECTED;
            order.cancellationReason = validationMessage;
            order.cancelledAt = LocalDateTime.now();
            
            // 🔥 PUBLISH ORDER_REJECTED EVENT
            eventProducer.publishOrderRejected(order.id, order.customerId, validationMessage);
        }

        order.persist();
    }

    public OrderResponseDto getOrderById(UUID orderId) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        return OrderResponseDto.from(order);
    }

    public List<OrderResponseDto> getOrdersByCustomer(UUID customerId) {
        return Order.findByCustomerId(customerId)
                .stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDto> getOrdersByCook(UUID cookId) {
        return Order.findByCookId(cookId)
                .stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDto> getActiveOrdersByCustomer(UUID customerId) {
        return Order.findActiveOrdersByCustomer(customerId)
                .stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDto> getActiveOrdersByCook(UUID cookId) {
        return Order.findActiveOrdersByCook(cookId)
                .stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }

        validateStatusTransition(order.status, newStatus);
        OrderStatus oldStatus = order.status;
        order.status = newStatus;
        
        // Update timestamp based on status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case PREPARING:
                order.preparedAt = now;
                break;
            case READY_FOR_PICKUP:
                break;
            case OUT_FOR_DELIVERY:
                order.pickedUpAt = now;
                break;
            case DELIVERED:
                order.deliveredAt = now;
                order.paymentStatus = PaymentStatus.COMPLETED;
                break;
            case CANCELLED:
                order.cancelledAt = now;
                break;
        }

        order.persist();

        // 🔥 PUBLISH ORDER_STATUS_UPDATED EVENT
        eventProducer.publishOrderStatusUpdated(order.id, oldStatus.toString(), newStatus.toString(), 
                                               order.customerId, order.cookId);

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, String cancellationReason) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }

        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED || order.status == OrderStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel order in current status: " + order.status);
        }

        OrderStatus oldStatus = order.status;
        order.status = OrderStatus.CANCELLED;
        order.cancellationReason = cancellationReason;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

        // 🔥 PUBLISH ORDER_CANCELLED EVENT
        eventProducer.publishOrderCancelled(order.id, order.customerId, order.cookId, cancellationReason);

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto addOrderReview(UUID orderId, BigDecimal rating, String reviewComment) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }

        if (order.status != OrderStatus.DELIVERED) {
            throw new BadRequestException("Can only review delivered orders");
        }

        order.rating = rating;
        order.reviewComment = reviewComment;
        order.persist();

        // 🔥 PUBLISH ORDER_REVIEWED EVENT
        eventProducer.publishOrderReviewed(order.id, order.customerId, order.cookId, rating, reviewComment);

        return OrderResponseDto.from(order);
    }

    // Helper methods
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValidTransition = false;

        switch (currentStatus) {
            case PENDING:
                isValidTransition = newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.REJECTED;
                break;
            case CONFIRMED:
                isValidTransition = newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
                break;
            case PREPARING:
                isValidTransition = newStatus == OrderStatus.READY_FOR_PICKUP || newStatus == OrderStatus.CANCELLED;
                break;
            case READY_FOR_PICKUP:
                isValidTransition = newStatus == OrderStatus.OUT_FOR_DELIVERY || newStatus == OrderStatus.DELIVERED;
                break;
            case OUT_FOR_DELIVERY:
                isValidTransition = newStatus == OrderStatus.DELIVERED;
                break;
            default:
                isValidTransition = false;
        }

        if (!isValidTransition) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private LocalDateTime calculateEstimatedDeliveryTime(DeliveryType deliveryType) {
        LocalDateTime now = LocalDateTime.now();
        if (deliveryType == DeliveryType.PICKUP) {
            return now.plusMinutes(30); // 30 minutes for pickup
        } else {
            return now.plusMinutes(60); // 60 minutes for delivery
        }
    }

    // Statistics methods (remain synchronous as they query local data)
    public long getTotalOrderCount() {
        return Order.count();
    }

    public long getOrderCountByStatus(OrderStatus status) {
        return Order.count("status", status);
    }

    public long getOrderCountByCustomer(UUID customerId) {
        return Order.count("customerId", customerId);
    }

    public long getOrderCountByCook(UUID cookId) {
        return Order.count("cookId", cookId);
    }

    public BigDecimal getTotalRevenue() {
        Object result = Order.find("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = ?1", OrderStatus.DELIVERED)
                .project(BigDecimal.class)
                .firstResult();
        return result != null ? (BigDecimal) result : BigDecimal.ZERO;
    }
}
