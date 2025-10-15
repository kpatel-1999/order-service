package org.example.orderservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import org.example.orderservice.dto.request.CreateOrderRequest;
import org.example.orderservice.dto.response.CreateOrderResponse;
import org.example.orderservice.dto.response.OrderResponse;
import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @Operation(summary = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or bad request"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public org.example.orderservice.dto.ApiResponse<CreateOrderResponse> createOrder(
            @Parameter(description = "Customer ID", required = true)
            @RequestHeader("X-Customer-Id") String customerId,
            @Valid @RequestBody CreateOrderRequest createOrderRequest) {

        logger.info("Received request to create order for customerId={}", customerId);

        Order order = orderMapper.toEntity(createOrderRequest, customerId);

        Order createdOrder = orderService.createOrder(order);

        CreateOrderResponse response = orderMapper.toCreateOrderResponse(createdOrder);

        logger.info("Order created successfully for customerId={}, orderId={}",
                customerId, response.orderId());

        return new org.example.orderservice.dto.ApiResponse<>("Order placed successfully", response);
    }


    @Operation(summary = "Get order details by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{orderId}")
    public org.example.orderservice.dto.ApiResponse<OrderResponse> getOrderById( @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        logger.info("[Order Controller] Fetching order for  orderId={}", orderId);

        Order order = orderService.getOrderById(orderId);
        OrderResponse response = orderMapper.toGetOrderResponse(order);

        logger.info("[Order Controller] Successfully retrieved order for orderId={}, totalAmount={}",
                order.getId(), response.totalAmount());

        return new org.example.orderservice.dto.ApiResponse<>("Order retrieved successfully", response);
    }

    @Operation(summary = "Get all orders, optionally filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public org.example.orderservice.dto.ApiResponse<List<OrderResponse>> getAllOrders(
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) OrderStatus status) {

        logger.info("[Order Controller] Fetching all orders  status={}", status);

        List<Order> orders = orderService.getAllOrders(status);

        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toGetOrderResponse)
                .toList();

        logger.info("[Order Controller] Successfully retrieved {} orders status={}", responses.size(), status);

        return new org.example.orderservice.dto.ApiResponse<>("All orders retrieved successfully", responses);
    }

    @Operation(summary = "Cancel an order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cancellation failed"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{orderId}/cancel")
    public org.example.orderservice.dto.ApiResponse<Void> cancelOrder(
            @Parameter(description = "Customer ID", required = true)
            @RequestHeader("X-Customer-Id") String customerId,
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long orderId) {
        logger.info("Received request to cancel order with orderId={}", orderId);

        orderService.cancelOrder(orderId,customerId);

        logger.info("Order with orderId={} cancelled successfully", orderId);

        return new org.example.orderservice.dto.ApiResponse<>("Order cancelled successfully", null);
    }
}
