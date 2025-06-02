package com.foodmarketplace.user.resource;

import com.foodmarketplace.user.dto.UserRegistrationDto;
import com.foodmarketplace.user.dto.UserResponseDto;
import com.foodmarketplace.user.entity.User;
import com.foodmarketplace.user.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "User registration, authentication and profile management")
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Path("/register")
    @Operation(summary = "Register a new user", description = "Register a new cook or customer")
    public Response registerUser(@Valid UserRegistrationDto registrationDto) {
        UserResponseDto user = userService.registerUser(registrationDto);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    public Response getUserById(@PathParam("userId") UUID userId) {
        UserResponseDto user = userService.getUserById(userId);
        return Response.ok(user).build();
    }

    @GET
    @Path("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user details by email address")
    public Response getUserByEmail(@PathParam("email") String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        return Response.ok(user).build();
    }

    @GET
    @Path("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve all users with specific role")
    public Response getUsersByRole(@PathParam("role") User.UserRole role) {
        List<UserResponseDto> users = userService.getUsersByRole(role);
        return Response.ok(users).build();
    }

    @GET
    @Path("/city/{city}")
    @Operation(summary = "Get users by city", description = "Retrieve all users in specific city")
    public Response getUsersByCity(@PathParam("city") String city) {
        List<UserResponseDto> users = userService.getUsersByCity(city);
        return Response.ok(users).build();
    }

    @PUT
    @Path("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user account status")
    public Response updateUserStatus(@PathParam("userId") UUID userId, 
                                   @QueryParam("status") User.UserStatus status) {
        UserResponseDto user = userService.updateUserStatus(userId, status);
        return Response.ok(user).build();
    }

    @PUT
    @Path("/{userId}/verification")
    @Operation(summary = "Update verification status", description = "Update user verification status")
    public Response updateVerificationStatus(@PathParam("userId") UUID userId, 
                                           @QueryParam("status") User.VerificationStatus status) {
        UserResponseDto user = userService.updateVerificationStatus(userId, status);
        return Response.ok(user).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get user statistics", description = "Get platform user statistics")
    public Response getUserStats() {
        return Response.ok(new UserStats(
            userService.getTotalUserCount(),
            userService.getCookCount(),
            userService.getCustomerCount()
        )).build();
    }

    public static class UserStats {
        public long totalUsers;
        public long totalCooks;
        public long totalCustomers;

        public UserStats(long totalUsers, long totalCooks, long totalCustomers) {
            this.totalUsers = totalUsers;
            this.totalCooks = totalCooks;
            this.totalCustomers = totalCustomers;
        }
    }
}
