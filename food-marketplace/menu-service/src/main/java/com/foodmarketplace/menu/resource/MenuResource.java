package com.foodmarketplace.menu.resource;

import com.foodmarketplace.menu.dto.DishCreateDto;
import com.foodmarketplace.menu.dto.DishResponseDto;
import com.foodmarketplace.menu.entity.Dish.*;
import com.foodmarketplace.menu.service.MenuService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/menu")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Menu Management", description = "Dish and menu management operations")
public class MenuResource {

    @Inject
    MenuService menuService;

    @POST
    @Path("/dishes")
    @Operation(summary = "Create a new dish", description = "Add a new dish to the menu")
    public Response createDish(@Valid DishCreateDto dishDto) {
        DishResponseDto dish = menuService.createDish(dishDto);
        return Response.status(Response.Status.CREATED).entity(dish).build();
    }

    @GET
    @Path("/dishes/{dishId}")
    @Operation(summary = "Get dish by ID", description = "Retrieve dish details by dish ID")
    public Response getDishById(@PathParam("dishId") UUID dishId) {
        DishResponseDto dish = menuService.getDishById(dishId);
        return Response.ok(dish).build();
    }

    @GET
    @Path("/dishes/cook/{cookId}")
    @Operation(summary = "Get dishes by cook", description = "Retrieve all dishes by a specific cook")
    public Response getDishesByCook(@PathParam("cookId") UUID cookId) {
        List<DishResponseDto> dishes = menuService.getDishesByCook(cookId);
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/dishes/cuisine/{cuisineType}")
    @Operation(summary = "Get dishes by cuisine", description = "Retrieve dishes by cuisine type")
    public Response getDishesByCuisine(@PathParam("cuisineType") CuisineType cuisineType) {
        List<DishResponseDto> dishes = menuService.getDishesByCuisine(cuisineType);
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/dishes/category/{category}")
    @Operation(summary = "Get dishes by category", description = "Retrieve dishes by category")
    public Response getDishesByCategory(@PathParam("category") DishCategory category) {
        List<DishResponseDto> dishes = menuService.getDishesByCategory(category);
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/dishes/available")
    @Operation(summary = "Get available dishes", description = "Retrieve all currently available dishes")
    public Response getAvailableDishes() {
        List<DishResponseDto> dishes = menuService.getAvailableDishes();
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/dishes/search")
    @Operation(summary = "Search dishes", description = "Search dishes by name or description")
    public Response searchDishes(@QueryParam("q") String query) {
        List<DishResponseDto> dishes = menuService.searchDishes(query);
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/dishes/location/{city}/cuisine/{cuisineType}")
    @Operation(summary = "Get dishes by city and cuisine", description = "Retrieve dishes by city and cuisine type")
    public Response getDishesByCityAndCuisine(@PathParam("city") String city, 
                                            @PathParam("cuisineType") CuisineType cuisineType) {
        List<DishResponseDto> dishes = menuService.getDishesByCityAndCuisine(city, cuisineType);
        return Response.ok(dishes).build();
    }

    @PUT
    @Path("/dishes/{dishId}/status")
    @Operation(summary = "Update dish status", description = "Update the status of a dish")
    public Response updateDishStatus(@PathParam("dishId") UUID dishId, 
                                   @QueryParam("status") DishStatus status) {
        DishResponseDto dish = menuService.updateDishStatus(dishId, status);
        return Response.ok(dish).build();
    }

    @PUT
    @Path("/dishes/{dishId}/quantity")
    @Operation(summary = "Update dish quantity", description = "Update available quantity of a dish")
    public Response updateDishQuantity(@PathParam("dishId") UUID dishId, 
                                     @QueryParam("quantity") Integer quantity) {
        DishResponseDto dish = menuService.updateDishQuantity(dishId, quantity);
        return Response.ok(dish).build();
    }

    @PUT
    @Path("/dishes/{dishId}/rating")
    @Operation(summary = "Update dish rating", description = "Add a new rating to a dish")
    public Response updateDishRating(@PathParam("dishId") UUID dishId, 
                                   @QueryParam("rating") Double rating) {
        if (rating < 1.0 || rating > 5.0) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Rating must be between 1.0 and 5.0").build();
        }
        
        menuService.updateDishRating(dishId, rating);
        return Response.ok().build();
    }

    @PUT
    @Path("/dishes/{dishId}/order")
    @Operation(summary = "Record dish order", description = "Increment order count and update quantity")
    public Response recordDishOrder(@PathParam("dishId") UUID dishId) {
        menuService.incrementOrderCount(dishId);
        return Response.ok().build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get menu statistics", description = "Get platform menu statistics")
    public Response getMenuStats() {
        return Response.ok(new MenuStats(
            menuService.getTotalDishCount(),
            menuService.getActiveDishCount()
        )).build();
    }

    @GET
    @Path("/stats/cook/{cookId}")
    @Operation(summary = "Get cook menu statistics", description = "Get menu statistics for a specific cook")
    public Response getCookMenuStats(@PathParam("cookId") UUID cookId) {
        return Response.ok(new CookMenuStats(
            cookId,
            menuService.getDishCountByCook(cookId)
        )).build();
    }

    @GET
    @Path("/stats/cuisine/{cuisineType}")
    @Operation(summary = "Get cuisine statistics", description = "Get statistics for a specific cuisine")
    public Response getCuisineStats(@PathParam("cuisineType") CuisineType cuisineType) {
        return Response.ok(new CuisineStats(
            cuisineType,
            menuService.getDishCountByCuisine(cuisineType)
        )).build();
    }

    public static class MenuStats {
        public long totalDishes;
        public long activeDishes;

        public MenuStats(long totalDishes, long activeDishes) {
            this.totalDishes = totalDishes;
            this.activeDishes = activeDishes;
        }
    }

    public static class CookMenuStats {
        public UUID cookId;
        public long totalDishes;

        public CookMenuStats(UUID cookId, long totalDishes) {
            this.cookId = cookId;
            this.totalDishes = totalDishes;
        }
    }

    public static class CuisineStats {
        public CuisineType cuisineType;
        public long totalDishes;

        public CuisineStats(CuisineType cuisineType, long totalDishes) {
            this.cuisineType = cuisineType;
            this.totalDishes = totalDishes;
        }
    }
}
