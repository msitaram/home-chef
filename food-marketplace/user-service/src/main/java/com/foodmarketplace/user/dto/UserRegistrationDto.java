package com.foodmarketplace.user.dto;

import com.foodmarketplace.user.entity.User.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {

    @NotBlank
    @Size(min = 2, max = 100)
    public String fullName;

    @Email
    @NotBlank
    public String email;

    @Pattern(regexp = "^[+]?[91]?[6-9][0-9]{9}$", message = "Invalid Indian mobile number")
    public String phone;

    @NotNull
    public UserRole role;

    @NotBlank
    public String addressLine1;

    public String addressLine2;

    @NotBlank
    public String city;

    @NotBlank
    public String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code")
    public String pincode;

    // Cook-specific fields
    public String specialityCuisine;

    @Pattern(regexp = "^[0-9]{12}$", message = "Invalid Aadhaar number")
    public String aadhaarNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number")
    public String panNumber;
}
