package com.foodmarketplace.order.service;

import com.foodmarketplace.order.client.DishDto;
import com.foodmarketplace.order.client.MenuServiceClient;
import com.foodmarketplace.order.dto.OrderCreateDto;
import com.foodmarketplace.order.dto.OrderResponseDto;
import com.foodmarketplace.order.entity.Order;
import com.foodmarketplace.order.entity.Order.*;
import com.foodmarketplace.order.entity.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    @Inject
    @RestClient
    MenuServiceClient menuServiceClient;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto orderDto) {
        // Validate delivery address for delivery type
        if (orderDto.deliveryType == DeliveryType.DELIVERY) {
            if (orderDto.deliveryAddress == null || orderDto.deliveryAddress.trim().isEmpty()) {
                throw new BadRequestException("Delivery address is required for delivery orders");
            }
            if (orderDto.deliveryCity == null || orderDto.deliveryCity.trim().isEmpty()) {
                throw new BadRequestException("Delivery city is required for delivery orders");
            }
            if (orderDto.deliveryPincode == null || orderDto.deliveryPincode.trim().isEmpty()) {
                throw new BadRequestException("Delivery pincode is required for delivery orders");
            }
        }

        // Create order
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

        // Process order items and calculate totals
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        UUID cookId = null;

        for (OrderCreateDto.OrderItemDto itemDto : orderDto.items) {
            // Get dish details from menu service
            DishDto dish;
            try {
                dish = menuServiceClient.getDishById(itemDto.dishId);
            } catch (Exception e) {
                throw new BadRequestException("Dish not found: " + itemDto.dishId);
            }

            // Validate dish availability
            if (!"ACTIVE".equals(dish.status)) {
                throw new BadRequestException("Dish is not available: " + dish.name);
            }

            if (dish.availableQuantity < itemDto.quantity) {
                throw new BadRequestException("Insufficient quantity available for dish: " + dish.name);
            }

            // Set cook ID from first dish (all dishes in one order must be from same cook)
            if (cookId == null) {
                cookId = dish.cookId;
            } else if (!cookId.equals(dish.cookId)) {
                throw new BadRequestException("All dishes in an order must be from the same cook");
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.order = order;
            orderItem.dishId = dish.id;
            orderItem.dishName = dish.name;
            orderItem.unitPrice = dish.price;
            orderItem.quantity = itemDto.quantity;
            orderItem.specialInstructions = itemDto.specialInstructions;
            orderItem.calculateTotalPrice();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.totalPrice);

            // Update dish quantity in menu service
            try {
                menuServiceClient.recordDishOrder(dish.id);
            } catch (Exception e) {
                // Log error but don't fail the order
                System.err.println("Failed to update dish quantity: " + e.getMessage());
            }
        }

        order.cookId = cookId;
        order.orderItems = orderItems;

        // Calculate delivery fee and tax
        if (order.deliveryType == DeliveryType.DELIVERY) {
            order.deliveryFee = calculateDeliveryFee(totalAmount);
            totalAmount = totalAmount.add(order.deliveryFee);
        }

        order.taxAmount = calculateTax(totalAmount);
        totalAmount = totalAmount.add(order.taxAmount);
        order.totalAmount = totalAmount;

        // Calculate estimated delivery time
        order.estimatedDeliveryTime = calculateEstimatedDeliveryTime(order.deliveryType);

        order.persist();

        return OrderResponseDto.from(order);
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

        order.status = newStatus;
        
        // Update timestamp based on status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED:
                order.confirmedAt = now;
                break;
            case PREPARING:
                // No specific timestamp for preparing
                break;
            case READY_FOR_PICKUP:
                order.preparedAt = now;
                break;
            case OUT_FOR_DELIVERY:
                order.pickedUpAt = now;
                break;
            case DELIVERED:
                order.deliveredAt = now;
                order.paymentStatus = PaymentStatus.COMPLETED;
                break;
            case CANCELLED:
            case REJECTED:
                order.cancelledAt = now;
                break;
        }

        order.persist();
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

        order.status = OrderStatus.CANCELLED;
        order.cancellationReason = cancellationReason;
        order.cancelledAt = LocalDateTime.now();
        order.persist();

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

        if (rating.doubleValue() < 1.0 || rating.doubleValue() > 5.0) {
            throw new BadRequestException("Rating must be between 1.0 and 5.0");
        }

        order.rating = rating;
        order.reviewComment = reviewComment;
        order.persist();

        return OrderResponseDto.from(order);
    }

    // Helper methods
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions
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

    private BigDecimal calculateDeliveryFee(BigDecimal orderAmount) {
        // Simple delivery fee calculation - 10% of order amount with min 20 and max 100
        BigDecimal deliveryFee = orderAmount.multiply(new BigDecimal("0.10"));
        BigDecimal minFee = new BigDecimal("20.00");
        BigDecimal maxFee = new BigDecimal("100.00");

        if (deliveryFee.compareTo(minFee) < 0) {
            return minFee;
        } else if (deliveryFee.compareTo(maxFee) > 0) {
            return maxFee;
        }
        return deliveryFee;
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        // 5% GST
        return amount.multiply(new BigDecimal("0.05"));
    }

    private LocalDateTime calculateEstimatedDeliveryTime(DeliveryType deliveryType) {
        LocalDateTime now = LocalDateTime.now();
        if (deliveryType == DeliveryType.PICKUP) {
            return now.plusMinutes(30); // 30 minutes for pickup
        } else {
            return now.plusMinutes(60); // 60 minutes for delivery
        }
    }

    // Statistics methods
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
