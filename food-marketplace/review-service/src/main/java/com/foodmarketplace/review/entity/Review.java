package com.foodmarketplace.review.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    public UUID orderId;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    public UUID customerId;

    @NotNull
    @Column(name = "cook_id", nullable = false)
    public UUID cookId;

    @NotNull
    @Column(name = "dish_id", nullable = false)
    public UUID dishId;

    @NotNull
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    public BigDecimal rating;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    public String comment;

    @Column(name = "food_quality_rating", precision = 3, scale = 2)
    public BigDecimal foodQualityRating;

    @Column(name = "delivery_rating", precision = 3, scale = 2)
    public BigDecimal deliveryRating;

    @Column(name = "packaging_rating", precision = 3, scale = 2)
    public BigDecimal packagingRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    public ReviewStatus reviewStatus = ReviewStatus.ACTIVE;

    @Column(name = "helpful_count")
    public Integer helpfulCount = 0;

    @Column(name = "reported_count")
    public Integer reportedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status")
    public ModerationStatus moderationStatus = ModerationStatus.PENDING;

    @Column(name = "moderation_reason", length = 500)
    public String moderationReason;

    @Column(name = "cook_response", length = 1000)
    public String cookResponse;

    @Column(name = "cook_response_at")
    public LocalDateTime cookResponseAt;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Static finder methods
    public static List<Review> findByCustomerId(UUID customerId) {
        return find("customerId", customerId).list();
    }

    public static List<Review> findByCookId(UUID cookId) {
        return find("cookId", cookId).list();
    }

    public static List<Review> findByDishId(UUID dishId) {
        return find("dishId", dishId).list();
    }

    public static List<Review> findByOrderId(UUID orderId) {
        return find("orderId", orderId).list();
    }

    public static List<Review> findActiveReviews() {
        return find("reviewStatus = ?1 and moderationStatus = ?2", 
                   ReviewStatus.ACTIVE, ModerationStatus.APPROVED).list();
    }

    public static List<Review> findPendingModeration() {
        return find("moderationStatus", ModerationStatus.PENDING).list();
    }

    public static double getAverageRatingForDish(UUID dishId) {
        Object result = find("SELECT AVG(r.rating) FROM Review r WHERE r.dishId = ?1 AND r.reviewStatus = ?2 AND r.moderationStatus = ?3", 
                           dishId, ReviewStatus.ACTIVE, ModerationStatus.APPROVED)
                .project(Double.class)
                .firstResult();
        return result != null ? (Double) result : 0.0;
    }

    public static double getAverageRatingForCook(UUID cookId) {
        Object result = find("SELECT AVG(r.rating) FROM Review r WHERE r.cookId = ?1 AND r.reviewStatus = ?2 AND r.moderationStatus = ?3", 
                           cookId, ReviewStatus.ACTIVE, ModerationStatus.APPROVED)
                .project(Double.class)
                .firstResult();
        return result != null ? (Double) result : 0.0;
    }

    public static long getReviewCountForDish(UUID dishId) {
        return count("dishId = ?1 and reviewStatus = ?2 and moderationStatus = ?3", 
                    dishId, ReviewStatus.ACTIVE, ModerationStatus.APPROVED);
    }

    public static long getReviewCountForCook(UUID cookId) {
        return count("cookId = ?1 and reviewStatus = ?2 and moderationStatus = ?3", 
                    cookId, ReviewStatus.ACTIVE, ModerationStatus.APPROVED);
    }

    public enum ReviewStatus {
        ACTIVE, HIDDEN, DELETED
    }

    public enum ModerationStatus {
        PENDING, APPROVED, REJECTED, FLAGGED
    }
}
