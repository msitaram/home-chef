package com.foodmarketplace.menu.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
public class Dish extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "name", nullable = false)
    public String name;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "description", nullable = false)
    public String description;

    @NotNull
    @Column(name = "cook_id", nullable = false)
    public UUID cookId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", nullable = false)
    public CuisineType cuisineType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    public DishCategory category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    public BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_type")
    public DietaryType dietaryType = DietaryType.VEGETARIAN;

    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level")
    public SpiceLevel spiceLevel = SpiceLevel.MEDIUM;

    @Column(name = "preparation_time_minutes")
    @Min(value = 5)
    @Max(value = 480)
    public Integer preparationTimeMinutes = 30;

    @NotNull
    @Min(value = 1)
    @Max(value = 50)
    @Column(name = "daily_capacity", nullable = false)
    public Integer dailyCapacity;

    @Min(value = 0)
    @Column(name = "available_quantity")
    public Integer availableQuantity = 0;

    @ElementCollection
    @CollectionTable(name = "dish_images", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "image_url")
    public List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "dish_ingredients", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "ingredient")
    public List<String> ingredients;

    @Column(name = "availability_start_time")
    public LocalTime availabilityStartTime;

    @Column(name = "availability_end_time")
    public LocalTime availabilityEndTime;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "dish_available_days", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "day_of_week")
    public List<DayOfWeek> availableDays;

    @Column(name = "average_rating", precision = 3, scale = 2)
    public BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    public Integer totalReviews = 0;

    @Column(name = "total_orders")
    public Integer totalOrders = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public DishStatus status = DishStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Static finder methods
    public static List<Dish> findByCookId(UUID cookId) {
        return find("cookId", cookId).list();
    }

    public static List<Dish> findByCuisineType(CuisineType cuisineType) {
        return find("cuisineType", cuisineType).list();
    }

    public static List<Dish> findByCategory(DishCategory category) {
        return find("category", category).list();
    }

    public static List<Dish> findAvailableDishes() {
        return find("status = ?1 and availableQuantity > 0", DishStatus.ACTIVE).list();
    }

    public static List<Dish> findByCityAndCuisine(String city, CuisineType cuisineType) {
        return find("select d from Dish d join User u on d.cookId = u.id " +
                   "where u.city = ?1 and d.cuisineType = ?2 and d.status = ?3", 
                   city, cuisineType, DishStatus.ACTIVE).list();
    }

    public enum CuisineType {
        NORTH_INDIAN, SOUTH_INDIAN, CHINESE, CONTINENTAL, 
        PUNJABI, GUJARATI, MAHARASHTRIAN, BENGALI, 
        RAJASTHANI, KERALA, TAMIL, ANDHRA, STREET_FOOD
    }

    public enum DishCategory {
        BREAKFAST, LUNCH, DINNER, SNACKS, DESSERTS, BEVERAGES
    }

    public enum DietaryType {
        VEGETARIAN, NON_VEGETARIAN, VEGAN, JAIN
    }

    public enum SpiceLevel {
        MILD, MEDIUM, SPICY, EXTRA_SPICY
    }

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public enum DishStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, TEMPORARILY_UNAVAILABLE
    }
}
