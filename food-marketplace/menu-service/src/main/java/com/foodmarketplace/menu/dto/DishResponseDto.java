package com.foodmarketplace.menu.dto;

import com.foodmarketplace.menu.entity.Dish;
import com.foodmarketplace.menu.entity.Dish.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class DishResponseDto {

    public UUID id;
    public String name;
    public String description;
    public UUID cookId;
    public CuisineType cuisineType;
    public DishCategory category;
    public BigDecimal price;
    public DietaryType dietaryType;
    public SpiceLevel spiceLevel;
    public Integer preparationTimeMinutes;
    public Integer dailyCapacity;
    public Integer availableQuantity;
    public List<String> imageUrls;
    public List<String> ingredients;
    public LocalTime availabilityStartTime;
    public LocalTime availabilityEndTime;
    public List<DayOfWeek> availableDays;
    public BigDecimal averageRating;
    public Integer totalReviews;
    public Integer totalOrders;
    public DishStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static DishResponseDto from(Dish dish) {
        DishResponseDto dto = new DishResponseDto();
        dto.id = dish.id;
        dto.name = dish.name;
        dto.description = dish.description;
        dto.cookId = dish.cookId;
        dto.cuisineType = dish.cuisineType;
        dto.category = dish.category;
        dto.price = dish.price;
        dto.dietaryType = dish.dietaryType;
        dto.spiceLevel = dish.spiceLevel;
        dto.preparationTimeMinutes = dish.preparationTimeMinutes;
        dto.dailyCapacity = dish.dailyCapacity;
        dto.availableQuantity = dish.availableQuantity;
        dto.imageUrls = dish.imageUrls;
        dto.ingredients = dish.ingredients;
        dto.availabilityStartTime = dish.availabilityStartTime;
        dto.availabilityEndTime = dish.availabilityEndTime;
        dto.availableDays = dish.availableDays;
        dto.averageRating = dish.averageRating;
        dto.totalReviews = dish.totalReviews;
        dto.totalOrders = dish.totalOrders;
        dto.status = dish.status;
        dto.createdAt = dish.createdAt;
        dto.updatedAt = dish.updatedAt;
        return dto;
    }
}
