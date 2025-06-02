package com.foodmarketplace.user.dto;

import com.foodmarketplace.user.entity.User;
import com.foodmarketplace.user.entity.User.UserRole;
import com.foodmarketplace.user.entity.User.UserStatus;
import com.foodmarketplace.user.entity.User.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponseDto {

    public UUID id;
    public String fullName;
    public String email;
    public String phone;
    public UserRole role;
    public UserStatus status;
    public String profileImageUrl;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String pincode;
    public String specialityCuisine;
    public VerificationStatus verificationStatus;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static UserResponseDto from(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.id = user.id;
        dto.fullName = user.fullName;
        dto.email = user.email;
        dto.phone = user.phone;
        dto.role = user.role;
        dto.status = user.status;
        dto.profileImageUrl = user.profileImageUrl;
        dto.addressLine1 = user.addressLine1;
        dto.addressLine2 = user.addressLine2;
        dto.city = user.city;
        dto.state = user.state;
        dto.pincode = user.pincode;
        dto.specialityCuisine = user.specialityCuisine;
        dto.verificationStatus = user.verificationStatus;
        dto.createdAt = user.createdAt;
        dto.updatedAt = user.updatedAt;
        return dto;
    }
}
