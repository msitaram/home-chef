package com.foodmarketplace.order.dto;

import com.foodmarketplace.order.entity.Order;
import com.foodmarketplace.order.entity.Order.*;
import com.foodmarketplace.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderResponseDto {

    public UUID id;
    public UUID customerId;
    public UUID cookId;
    public OrderStatus status;
    public BigDecimal totalAmount;
    public BigDecimal deliveryFee;
    public BigDecimal taxAmount;
    public DeliveryType deliveryType;
    public String deliveryAddress;
    public String deliveryCity;
    public String deliveryPincode;
    public String deliveryInstructions;
    public LocalDateTime estimatedDeliveryTime;
    public LocalDateTime actualDeliveryTime;
    public String paymentMethod;
    public String paymentTransactionId;
    public PaymentStatus paymentStatus;
    public String specialInstructions;
    public String cancellationReason;
    public BigDecimal rating;
    public String reviewComment;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime confirmedAt;
    public LocalDateTime preparedAt;
    public LocalDateTime pickedUpAt;
    public LocalDateTime deliveredAt;
    public LocalDateTime cancelledAt;
    public List<OrderItemResponseDto> orderItems;

    public static OrderResponseDto from(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.cookId = order.cookId;
        dto.status = order.status;
        dto.totalAmount = order.totalAmount;
        dto.deliveryFee = order.deliveryFee;
        dto.taxAmount = order.taxAmount;
        dto.deliveryType = order.deliveryType;
        dto.deliveryAddress = order.deliveryAddress;
        dto.deliveryCity = order.deliveryCity;
        dto.deliveryPincode = order.deliveryPincode;
        dto.deliveryInstructions = order.deliveryInstructions;
        dto.estimatedDeliveryTime = order.estimatedDeliveryTime;
        dto.actualDeliveryTime = order.actualDeliveryTime;
        dto.paymentMethod = order.paymentMethod;
        dto.paymentTransactionId = order.paymentTransactionId;
        dto.paymentStatus = order.paymentStatus;
        dto.specialInstructions = order.specialInstructions;
        dto.cancellationReason = order.cancellationReason;
        dto.rating = order.rating;
        dto.reviewComment = order.reviewComment;
        dto.createdAt = order.createdAt;
        dto.updatedAt = order.updatedAt;
        dto.confirmedAt = order.confirmedAt;
        dto.preparedAt = order.preparedAt;
        dto.pickedUpAt = order.pickedUpAt;
        dto.deliveredAt = order.deliveredAt;
        dto.cancelledAt = order.cancelledAt;
        
        if (order.orderItems != null) {
            dto.orderItems = order.orderItems.stream()
                    .map(OrderItemResponseDto::from)
                    .collect(Collectors.toList());
        }
        
        return dto;
    }

    public static class OrderItemResponseDto {
        public UUID id;
        public UUID dishId;
        public String dishName;
        public BigDecimal unitPrice;
        public Integer quantity;
        public BigDecimal totalPrice;
        public String specialInstructions;

        public static OrderItemResponseDto from(OrderItem item) {
            OrderItemResponseDto dto = new OrderItemResponseDto();
            dto.id = item.id;
            dto.dishId = item.dishId;
            dto.dishName = item.dishName;
            dto.unitPrice = item.unitPrice;
            dto.quantity = item.quantity;
            dto.totalPrice = item.totalPrice;
            dto.specialInstructions = item.specialInstructions;
            return dto;
        }
    }
}
