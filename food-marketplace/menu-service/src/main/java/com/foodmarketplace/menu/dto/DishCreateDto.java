package com.foodmarketplace.menu.dto;

import com.foodmarketplace.menu.entity.Dish.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class DishCreateDto {

    @NotBlank
    @Size(min = 2, max = 100)
    public String name;

    @NotBlank
    @Size(max = 1000)
    public String description;

    @NotNull
    public UUID cookId;

    @NotNull
    public CuisineType cuisineType;

    @NotNull
    public DishCategory category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    public BigDecimal price;

    public DietaryType dietaryType = DietaryType.VEGETARIAN;

    public SpiceLevel spiceLevel = SpiceLevel.MEDIUM;

    @Min(value = 5)
    @Max(value = 480)
    public Integer preparationTimeMinutes = 30;

    @NotNull
    @Min(value = 1)
    @Max(value = 50)
    public Integer dailyCapacity;

    public List<String> imageUrls;

    public List<String> ingredients;

    public LocalTime availabilityStartTime;

    public LocalTime availabilityEndTime;

    public List<DayOfWeek> availableDays;
}
