package com.foodmarketplace.user.service;

import com.foodmarketplace.user.dto.UserRegistrationDto;
import com.foodmarketplace.user.dto.UserResponseDto;
import com.foodmarketplace.user.entity.User;
import com.foodmarketplace.user.event.UserEventProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserEventProducer eventProducer;

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // Check for existing user
        if (User.findByEmail(registrationDto.email) != null) {
            throw new BadRequestException("Email already registered");
        }
        
        if (User.findByPhone(registrationDto.phone) != null) {
            throw new BadRequestException("Phone number already registered");
        }

        // Create new user
        User user = new User();
        user.fullName = registrationDto.fullName;
        user.email = registrationDto.email;
        user.phone = registrationDto.phone;
        user.role = registrationDto.role;
        user.addressLine1 = registrationDto.addressLine1;
        user.addressLine2 = registrationDto.addressLine2;
        user.city = registrationDto.city;
        user.state = registrationDto.state;
        user.pincode = registrationDto.pincode;

        // Cook-specific fields
        if (registrationDto.role == User.UserRole.COOK) {
            user.specialityCuisine = registrationDto.specialityCuisine;
            user.aadhaarNumber = registrationDto.aadhaarNumber;
            user.panNumber = registrationDto.panNumber;
        }

        user.persist();

        // Publish user registration event
        eventProducer.publishUserRegistered(user.id, user.email, user.role.toString());

        return UserResponseDto.from(user);
    }

    public UserResponseDto getUserById(UUID userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return UserResponseDto.from(user);
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = User.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return UserResponseDto.from(user);
    }

    public List<UserResponseDto> getUsersByRole(User.UserRole role) {
        return User.<User>find("role", role)
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<UserResponseDto> getUsersByCity(String city) {
        return User.<User>find("city", city)
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUserStatus(UUID userId, User.UserStatus status) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        user.status = status;
        user.persist();

        // Publish user status updated event
        eventProducer.publishUserStatusUpdated(user.id, status.toString());

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updateVerificationStatus(UUID userId, User.VerificationStatus verificationStatus) {
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        user.verificationStatus = verificationStatus;
        user.persist();

        // Publish verification status updated event
        eventProducer.publishUserVerificationUpdated(user.id, verificationStatus.toString());

        return UserResponseDto.from(user);
    }

    public long getTotalUserCount() {
        return User.count();
    }

    public long getCookCount() {
        return User.count("role", User.UserRole.COOK);
    }

    public long getCustomerCount() {
        return User.count("role", User.UserRole.CUSTOMER);
    }
}
