package com.foodmarketplace.order.dto;

import com.foodmarketplace.order.entity.Order.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public class OrderCreateDto {

    @NotNull
    public UUID customerId;

    @NotNull
    public DeliveryType deliveryType;

    @Valid
    @NotEmpty
    public List<OrderItemDto> items;

    @Size(max = 500)
    public String deliveryAddress;

    @Size(max = 100)
    public String deliveryCity;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code")
    public String deliveryPincode;

    @Size(max = 300)
    public String deliveryInstructions;

    @Size(max = 500)
    public String specialInstructions;

    @Size(max = 50)
    public String paymentMethod = "COD"; // Cash on Delivery default

    public static class OrderItemDto {
        @NotNull
        public UUID dishId;

        @NotNull
        @Min(value = 1)
        @Max(value = 10)
        public Integer quantity;

        @Size(max = 300)
        public String specialInstructions;
    }
}
