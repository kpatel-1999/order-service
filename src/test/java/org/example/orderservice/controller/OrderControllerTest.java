package org.example.orderservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.controllers.OrderController;
import org.example.orderservice.dto.ApiResponse;
import org.example.orderservice.dto.request.CreateOrderRequest;
import org.example.orderservice.dto.request.OrderItemRequest;
import org.example.orderservice.dto.response.CreateOrderResponse;
import org.example.orderservice.dto.response.OrderItemResponse;
import org.example.orderservice.dto.response.OrderResponse;
import org.example.orderservice.entities.Order;
import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.exception.OrderCancellationException;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.exception.UnAuthorizedException;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @Test
    void createOrderSuccessfully() throws Exception {
        String customerId = "123";
        OrderItemRequest orderItemRequest = new OrderItemRequest("Laptop", 1, 2, new BigDecimal("5000.00"));
        CreateOrderRequest request = new CreateOrderRequest(List.of(orderItemRequest));

        Order orderEntity = new Order();
        orderEntity.setId(1L);

        CreateOrderResponse responseDto = new CreateOrderResponse(1L);

        Mockito.when(orderMapper.toEntity(request, customerId)).thenReturn(orderEntity);
        Mockito.when(orderService.createOrder(orderEntity)).thenReturn(orderEntity);
        Mockito.when(orderMapper.toCreateOrderResponse(orderEntity)).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<CreateOrderResponse> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<CreateOrderResponse>>() {
                }
        );

        Assertions.assertEquals("Order placed successfully", apiResponse.message());
        Assertions.assertEquals(1L, apiResponse.data().orderId());
    }

    @Test
    void throwBadRequestWhenItemsAreEmpty() throws Exception {
        String customerId = "123";
        CreateOrderRequest request = new CreateOrderRequest(Collections.emptyList());

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiResponse<Map<String, String>> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<Map<String, String>>>() {
                }
        );

        Assertions.assertEquals("Validation failed", apiResponse.message());
        Assertions.assertEquals("Order must contain at least one item", apiResponse.data().get("items"));
    }

    @Test
    void throwBadRequestWhenItemsHaveInvalidInputs() throws Exception {
        String customerId = "123";
        OrderItemRequest invalidItem = new OrderItemRequest("", null, -2, new BigDecimal("0"));
        CreateOrderRequest request = new CreateOrderRequest(List.of(invalidItem));

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiResponse<Map<String, String>> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<Map<String, String>>>() {
                }
        );

        Assertions.assertEquals("Validation failed", apiResponse.message());
        Assertions.assertEquals("Product name is required", apiResponse.data().get("items[0].productName"));
        Assertions.assertEquals("Product ID is required", apiResponse.data().get("items[0].productId"));
        Assertions.assertEquals("Quantity should be greater than zero", apiResponse.data().get("items[0].productQuantity"));
        Assertions.assertEquals("Product price should be greater than zero", apiResponse.data().get("items[0].productPrice"));
    }

    @Test
    void retrieveOrderByIdSuccessfully() throws Exception {
        Long orderId = 1L;
        String customerId = "123";

        Order orderEntity = new Order();
        orderEntity.setId(orderId);
        orderEntity.setCustomerId(customerId);
        orderEntity.setStatus(OrderStatus.PENDING);

        OrderItemResponse itemResponse = new OrderItemResponse("Mouse", new BigDecimal("500.00"), 1, 1);
        OrderResponse responseDto = new OrderResponse(orderId, customerId, OrderStatus.PENDING,
                new BigDecimal("500.00"), Instant.now(), List.of(itemResponse));

        Mockito.when(orderService.getOrderById(orderId)).thenReturn(orderEntity);
        Mockito.when(orderMapper.toGetOrderResponse(orderEntity)).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<OrderResponse> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<OrderResponse>>() {
                }
        );

        Assertions.assertEquals("Order retrieved successfully", apiResponse.message());
        Assertions.assertEquals(orderId, apiResponse.data().orderId());
        Assertions.assertEquals(1, apiResponse.data().items().size());
    }

    @Test
    void throwNotFoundExceptionWhenOrderIsNotFound() throws Exception {
        Long orderId = 1L;
        Mockito.when(orderService.getOrderById(orderId))
                .thenThrow(new ResourceNotFoundException("Order Not Found"));

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data.error").value("Order Not Found"));
    }

    @Test
    void getAllOrdersSuccessfully() throws Exception {
        Order orderEntity = new Order();
        orderEntity.setId(1L);
        orderEntity.setCustomerId("123");
        orderEntity.setStatus(OrderStatus.PENDING);

        OrderItemResponse itemResponse = new OrderItemResponse("Mouse", new BigDecimal("500.00"), 1, 1);
        OrderResponse responseDto = new OrderResponse(1L, "123", OrderStatus.PENDING,
                new BigDecimal("500.00"), Instant.now(), List.of(itemResponse));

        Mockito.when(orderService.getAllOrders(null)).thenReturn(List.of(orderEntity));
        Mockito.when(orderMapper.toGetOrderResponse(orderEntity)).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<List<OrderResponse>> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<List<OrderResponse>>>() {
                }
        );

        Assertions.assertEquals("All orders retrieved successfully", apiResponse.message());
        Assertions.assertEquals(1, apiResponse.data().size());
    }

    @Test
    void cancelOrderSuccessfully() throws Exception {
        Long orderId = 1L;
        String customerId = "123";

        Mockito.doNothing().when(orderService).cancelOrder(orderId, customerId);

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", orderId)
                        .header("X-Customer-Id", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    void throwExceptionWhenOrderStatusNotPending() throws Exception {
        Long orderId = 1L;
        String customerId = "123";

        Mockito.doThrow(new OrderCancellationException("Order cannot be cancelled as it is already PROCESSING"))
                .when(orderService).cancelOrder(orderId, customerId);

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", orderId)
                        .header("X-Customer-Id", customerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order cancellation failed"))
                .andExpect(jsonPath("$.data.error").value("Order cannot be cancelled as it is already PROCESSING"));
    }

    @Test
    void throwExceptionWhenCustomerIsDifferent() throws Exception {
        Long orderId = 1L;
        String customerId = "123";

        Mockito.doThrow(new UnAuthorizedException("You are not authorized to cancel this order"))
                .when(orderService).cancelOrder(orderId, customerId);

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", orderId)
                        .header("X-Customer-Id", customerId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Unauthorized access"))
                .andExpect(jsonPath("$.data.error").value("You are not authorized to cancel this order"));
    }
}
