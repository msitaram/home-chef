package com.foodmarketplace.order.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public Order order;

    @NotNull
    @Column(name = "dish_id", nullable = false)
    public UUID dishId;

    @NotBlank
    @Column(name = "dish_name", nullable = false, length = 100)
    public String dishName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal unitPrice;

    @NotNull
    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalPrice;

    @Column(name = "special_instructions", length = 300)
    public String specialInstructions;

    // Calculate total price based on quantity and unit price
    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Static finder methods
    public static java.util.List<OrderItem> findByOrderId(UUID orderId) {
        return find("order.id", orderId).list();
    }

    public static java.util.List<OrderItem> findByDishId(UUID dishId) {
        return find("dishId", dishId).list();
    }
}
