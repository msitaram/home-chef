package com.foodmarketplace.order.client;

import java.math.BigDecimal;
import java.util.UUID;

public class DishDto {
    public UUID id;
    public String name;
    public UUID cookId;
    public BigDecimal price;
    public Integer availableQuantity;
    public String status;
}
