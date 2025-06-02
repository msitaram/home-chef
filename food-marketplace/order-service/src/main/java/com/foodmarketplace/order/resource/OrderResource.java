package com.foodmarketplace.order.resource;

import com.foodmarketplace.order.dto.OrderCreateDto;
import com.foodmarketplace.order.dto.OrderResponseDto;
import com.foodmarketplace.order.entity.Order.OrderStatus;
import com.foodmarketplace.order.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Order Management", description = "Order processing and management operations")
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    @Operation(summary = "Create a new order", description = "Place a new order with multiple dishes")
    public Response createOrder(@Valid OrderCreateDto orderDto) {
        OrderResponseDto order = orderService.createOrder(orderDto);
        return Response.status(Response.Status.CREATED).entity(order).build();
    }

    @GET
    @Path("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID")
    public Response getOrderById(@PathParam("orderId") UUID orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        return Response.ok(order).build();
    }

    @GET
    @Path("/customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Retrieve all orders for a specific customer")
    public Response getOrdersByCustomer(@PathParam("customerId") UUID customerId) {
        List<OrderResponseDto> orders = orderService.getOrdersByCustomer(customerId);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/cook/{cookId}")
    @Operation(summary = "Get orders by cook", description = "Retrieve all orders for a specific cook")
    public Response getOrdersByCook(@PathParam("cookId") UUID cookId) {
        List<OrderResponseDto> orders = orderService.getOrdersByCook(cookId);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/customer/{customerId}/active")
    @Operation(summary = "Get active orders by customer", description = "Retrieve active orders for a specific customer")
    public Response getActiveOrdersByCustomer(@PathParam("customerId") UUID customerId) {
        List<OrderResponseDto> orders = orderService.getActiveOrdersByCustomer(customerId);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/cook/{cookId}/active")
    @Operation(summary = "Get active orders by cook", description = "Retrieve active orders for a specific cook")
    public Response getActiveOrdersByCook(@PathParam("cookId") UUID cookId) {
        List<OrderResponseDto> orders = orderService.getActiveOrdersByCook(cookId);
        return Response.ok(orders).build();
    }

    @PUT
    @Path("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public Response updateOrderStatus(@PathParam("orderId") UUID orderId, 
                                    @QueryParam("status") OrderStatus status) {
        OrderResponseDto order = orderService.updateOrderStatus(orderId, status);
        return Response.ok(order).build();
    }

    @PUT
    @Path("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order with reason")
    public Response cancelOrder(@PathParam("orderId") UUID orderId, 
                              @QueryParam("reason") String reason) {
        OrderResponseDto order = orderService.cancelOrder(orderId, reason);
        return Response.ok(order).build();
    }

    @PUT
    @Path("/{orderId}/review")
    @Operation(summary = "Add order review", description = "Add rating and review to a delivered order")
    public Response addOrderReview(@PathParam("orderId") UUID orderId, 
                                 @QueryParam("rating") Double rating,
                                 @QueryParam("comment") String comment) {
        if (rating < 1.0 || rating > 5.0) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Rating must be between 1.0 and 5.0").build();
        }
        
        OrderResponseDto order = orderService.addOrderReview(orderId, 
                                                           BigDecimal.valueOf(rating), 
                                                           comment);
        return Response.ok(order).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get order statistics", description = "Get platform order statistics")
    public Response getOrderStats() {
        return Response.ok(new OrderStats(
            orderService.getTotalOrderCount(),
            orderService.getOrderCountByStatus(OrderStatus.PENDING),
            orderService.getOrderCountByStatus(OrderStatus.CONFIRMED),
            orderService.getOrderCountByStatus(OrderStatus.PREPARING),
            orderService.getOrderCountByStatus(OrderStatus.OUT_FOR_DELIVERY),
            orderService.getOrderCountByStatus(OrderStatus.DELIVERED),
            orderService.getOrderCountByStatus(OrderStatus.CANCELLED),
            orderService.getTotalRevenue()
        )).build();
    }

    @GET
    @Path("/stats/customer/{customerId}")
    @Operation(summary = "Get customer order statistics", description = "Get order statistics for a specific customer")
    public Response getCustomerOrderStats(@PathParam("customerId") UUID customerId) {
        return Response.ok(new CustomerOrderStats(
            customerId,
            orderService.getOrderCountByCustomer(customerId)
        )).build();
    }

    @GET
    @Path("/stats/cook/{cookId}")
    @Operation(summary = "Get cook order statistics", description = "Get order statistics for a specific cook")
    public Response getCookOrderStats(@PathParam("cookId") UUID cookId) {
        return Response.ok(new CookOrderStats(
            cookId,
            orderService.getOrderCountByCook(cookId)
        )).build();
    }

    public static class OrderStats {
        public long totalOrders;
        public long pendingOrders;
        public long confirmedOrders;
        public long preparingOrders;
        public long outForDeliveryOrders;
        public long deliveredOrders;
        public long cancelledOrders;
        public BigDecimal totalRevenue;

        public OrderStats(long totalOrders, long pendingOrders, long confirmedOrders, 
                         long preparingOrders, long outForDeliveryOrders, 
                         long deliveredOrders, long cancelledOrders, BigDecimal totalRevenue) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.confirmedOrders = confirmedOrders;
            this.preparingOrders = preparingOrders;
            this.outForDeliveryOrders = outForDeliveryOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalRevenue = totalRevenue;
        }
    }

    public static class CustomerOrderStats {
        public UUID customerId;
        public long totalOrders;

        public CustomerOrderStats(UUID customerId, long totalOrders) {
            this.customerId = customerId;
            this.totalOrders = totalOrders;
        }
    }

    public static class CookOrderStats {
        public UUID cookId;
        public long totalOrders;

        public CookOrderStats(UUID cookId, long totalOrders) {
            this.cookId = cookId;
            this.totalOrders = totalOrders;
        }
    }
}
