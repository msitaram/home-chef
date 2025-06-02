package com.foodmarketplace.order.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@RegisterRestClient
@Path("/api/menu")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MenuServiceClient {

    @GET
    @Path("/dishes/{dishId}")
    DishDto getDishById(@PathParam("dishId") UUID dishId);

    @PUT
    @Path("/dishes/{dishId}/order")
    void recordDishOrder(@PathParam("dishId") UUID dishId);
}
