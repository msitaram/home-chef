package com.foodmarketplace.menu.service;

import com.foodmarketplace.menu.dto.DishCreateDto;
import com.foodmarketplace.menu.dto.DishResponseDto;
import com.foodmarketplace.menu.entity.Dish;
import com.foodmarketplace.menu.entity.Dish.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MenuService {

    @Transactional
    public DishResponseDto createDish(DishCreateDto dishDto) {
        // Create new dish
        Dish dish = new Dish();
        dish.name = dishDto.name;
        dish.description = dishDto.description;
        dish.cookId = dishDto.cookId;
        dish.cuisineType = dishDto.cuisineType;
        dish.category = dishDto.category;
        dish.price = dishDto.price;
        dish.dietaryType = dishDto.dietaryType != null ? dishDto.dietaryType : DietaryType.VEGETARIAN;
        dish.spiceLevel = dishDto.spiceLevel != null ? dishDto.spiceLevel : SpiceLevel.MEDIUM;
        dish.preparationTimeMinutes = dishDto.preparationTimeMinutes != null ? dishDto.preparationTimeMinutes : 30;
        dish.dailyCapacity = dishDto.dailyCapacity;
        dish.availableQuantity = dishDto.dailyCapacity; // Initially set to full capacity
        dish.imageUrls = dishDto.imageUrls;
        dish.ingredients = dishDto.ingredients;
        dish.availabilityStartTime = dishDto.availabilityStartTime;
        dish.availabilityEndTime = dishDto.availabilityEndTime;
        dish.availableDays = dishDto.availableDays;

        dish.persist();
        return DishResponseDto.from(dish);
    }

    public DishResponseDto getDishById(UUID dishId) {
        Dish dish = Dish.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish not found");
        }
        return DishResponseDto.from(dish);
    }

    public List<DishResponseDto> getDishesByCook(UUID cookId) {
        return Dish.findByCookId(cookId)
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DishResponseDto> getDishesByCuisine(CuisineType cuisineType) {
        return Dish.findByCuisineType(cuisineType)
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DishResponseDto> getDishesByCategory(DishCategory category) {
        return Dish.findByCategory(category)
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DishResponseDto> getAvailableDishes() {
        return Dish.findAvailableDishes()
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DishResponseDto> searchDishes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAvailableDishes();
        }
        
        String searchQuery = "%" + query.toLowerCase() + "%";
        return Dish.<Dish>find("lower(name) like ?1 or lower(description) like ?1", searchQuery)
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DishResponseDto> getDishesByCityAndCuisine(String city, CuisineType cuisineType) {
        return Dish.findByCityAndCuisine(city, cuisineType)
                .stream()
                .map(DishResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public DishResponseDto updateDishStatus(UUID dishId, DishStatus status) {
        Dish dish = Dish.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish not found");
        }

        dish.status = status;
        dish.persist();
        return DishResponseDto.from(dish);
    }

    @Transactional
    public DishResponseDto updateDishQuantity(UUID dishId, Integer quantity) {
        Dish dish = Dish.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish not found");
        }

        if (quantity < 0 || quantity > dish.dailyCapacity) {
            throw new BadRequestException("Invalid quantity. Must be between 0 and daily capacity");
        }

        dish.availableQuantity = quantity;
        
        // Auto-update status based on quantity
        if (quantity == 0) {
            dish.status = DishStatus.OUT_OF_STOCK;
        } else if (dish.status == DishStatus.OUT_OF_STOCK) {
            dish.status = DishStatus.ACTIVE;
        }

        dish.persist();
        return DishResponseDto.from(dish);
    }

    @Transactional
    public void updateDishRating(UUID dishId, Double newRating) {
        Dish dish = Dish.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish not found");
        }

        // Simple rating calculation - in production, this would be more sophisticated
        if (dish.totalReviews == 0) {
            dish.averageRating = java.math.BigDecimal.valueOf(newRating);
        } else {
            double currentTotal = dish.averageRating.doubleValue() * dish.totalReviews;
            double newTotal = currentTotal + newRating;
            dish.averageRating = java.math.BigDecimal.valueOf(newTotal / (dish.totalReviews + 1));
        }
        
        dish.totalReviews += 1;
        dish.persist();
    }

    @Transactional
    public void incrementOrderCount(UUID dishId) {
        Dish dish = Dish.findById(dishId);
        if (dish == null) {
            throw new NotFoundException("Dish not found");
        }

        dish.totalOrders += 1;
        if (dish.availableQuantity > 0) {
            dish.availableQuantity -= 1;
        }

        if (dish.availableQuantity == 0) {
            dish.status = DishStatus.OUT_OF_STOCK;
        }

        dish.persist();
    }

    // Statistics methods
    public long getTotalDishCount() {
        return Dish.count();
    }

    public long getActiveDishCount() {
        return Dish.count("status", DishStatus.ACTIVE);
    }

    public long getDishCountByCook(UUID cookId) {
        return Dish.count("cookId", cookId);
    }

    public long getDishCountByCuisine(CuisineType cuisineType) {
        return Dish.count("cuisineType", cuisineType);
    }
}
