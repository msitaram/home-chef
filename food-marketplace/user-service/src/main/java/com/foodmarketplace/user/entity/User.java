package com.foodmarketplace.user.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id")
    public UUID id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @Pattern(regexp = "^[+]?[91]?[6-9][0-9]{9}$", message = "Invalid Indian mobile number")
    @Column(name = "phone", nullable = false, unique = true)
    public String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    public UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "profile_image_url")
    public String profileImageUrl;

    @Column(name = "address_line1")
    public String addressLine1;

    @Column(name = "address_line2")
    public String addressLine2;

    @Column(name = "city", nullable = false)
    public String city;

    @Column(name = "state", nullable = false)
    public String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code")
    @Column(name = "pincode", nullable = false)
    public String pincode;

    // Cook-specific fields
    @Column(name = "speciality_cuisine")
    public String specialityCuisine;

    @Column(name = "aadhaar_number")
    @Pattern(regexp = "^[0-9]{12}$", message = "Invalid Aadhaar number")
    public String aadhaarNumber;

    @Column(name = "pan_number")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number")
    public String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    public VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Static finder methods
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static User findByPhone(String phone) {
        return find("phone", phone).firstResult();
    }

    public enum UserRole {
        CUSTOMER, COOK, ADMIN
    }

    public enum UserStatus {
        PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED
    }

    public enum VerificationStatus {
        NOT_VERIFIED, AADHAAR_VERIFIED, PAN_VERIFIED, FULLY_VERIFIED
    }
}
