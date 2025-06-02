package com.foodmarketplace.order.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    public UUID customerId;

    @NotNull
    @Column(name = "cook_id", nullable = false)
    public UUID cookId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public OrderStatus status = OrderStatus.PENDING;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "delivery_fee", precision = 8, scale = 2)
    public BigDecimal deliveryFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @Column(name = "tax_amount", precision = 8, scale = 2)
    public BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    public DeliveryType deliveryType;

    @Column(name = "delivery_address", length = 500)
    public String deliveryAddress;

    @Column(name = "delivery_city", length = 100)
    public String deliveryCity;

    @Column(name = "delivery_pincode", length = 10)
    public String deliveryPincode;

    @Column(name = "delivery_instructions", length = 300)
    public String deliveryInstructions;

    @Column(name = "estimated_delivery_time")
    public LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    public LocalDateTime actualDeliveryTime;

    @Column(name = "payment_method", length = 50)
    public String paymentMethod;

    @Column(name = "payment_transaction_id", length = 100)
    public String paymentTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    public PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "special_instructions", length = 500)
    public String specialInstructions;

    @Column(name = "cancellation_reason", length = 300)
    public String cancellationReason;

    @Column(name = "rating", precision = 3, scale = 2)
    public BigDecimal rating;

    @Column(name = "review_comment", length = 500)
    public String reviewComment;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    public LocalDateTime confirmedAt;

    @Column(name = "prepared_at")
    public LocalDateTime preparedAt;

    @Column(name = "picked_up_at")
    public LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    public LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    public LocalDateTime cancelledAt;

    // One-to-many relationship with order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<OrderItem> orderItems;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Static finder methods
    public static List<Order> findByCustomerId(UUID customerId) {
        return find("customerId", customerId).list();
    }

    public static List<Order> findByCookId(UUID cookId) {
        return find("cookId", cookId).list();
    }

    public static List<Order> findByStatus(OrderStatus status) {
        return find("status", status).list();
    }

    public static List<Order> findActiveOrdersByCustomer(UUID customerId) {
        return find("customerId = ?1 and status not in (?2, ?3, ?4)", 
                   customerId, OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REJECTED).list();
    }

    public static List<Order> findActiveOrdersByCook(UUID cookId) {
        return find("cookId = ?1 and status not in (?2, ?3, ?4)", 
                   cookId, OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REJECTED).list();
    }

    public static List<Order> findOrdersInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return find("createdAt >= ?1 and createdAt <= ?2", startDate, endDate).list();
    }

    public enum OrderStatus {
        PENDING,           // Order placed, waiting for cook confirmation
        CONFIRMED,         // Cook confirmed the order
        PREPARING,         // Cook is preparing the food
        READY_FOR_PICKUP,  // Food is ready for pickup/delivery
        OUT_FOR_DELIVERY,  // Order is out for delivery
        DELIVERED,         // Order successfully delivered
        CANCELLED,         // Order cancelled by customer
        REJECTED           // Order rejected by cook
    }

    public enum DeliveryType {
        DELIVERY, PICKUP
    }

    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    }
}
