package org.example.orderservice.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.dto.ApiResponse;
import org.example.orderservice.dto.request.CreateOrderRequest;
import org.example.orderservice.dto.request.OrderItemRequest;
import org.example.orderservice.dto.response.CreateOrderResponse;
import org.example.orderservice.dto.response.OrderResponse;
import org.example.orderservice.entities.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        String customerId = "123";

        OrderItemRequest itemRequest = new OrderItemRequest("Laptop", 1, 2, new BigDecimal("5000.00"));
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andReturn();

        ApiResponse<CreateOrderResponse> apiResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<CreateOrderResponse>>() {}
        );

        Long createdOrderId = apiResponse.data().orderId();
        assertNotNull(createdOrderId);
        assertTrue(orderRepository.findById(createdOrderId).isPresent());
    }

    @Test
    void shouldRetrieveOrderByIdSuccessfully() throws Exception {
        String customerId = "123";
        OrderItemRequest itemRequest = new OrderItemRequest("Laptop", 1, 2, new BigDecimal("5000.00"));
        CreateOrderRequest createRequest = new CreateOrderRequest(List.of(itemRequest));

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        ApiResponse<CreateOrderResponse> createdOrder = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<CreateOrderResponse>>() {}
        );

        Long orderId = createdOrder.data().orderId();

        MvcResult getResult = mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andReturn();

        ApiResponse<OrderResponse> orderResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<OrderResponse>>() {}
        );

        assertEquals(orderId, orderResponse.data().orderId());
        assertEquals(customerId, orderResponse.data().customerId());
        assertEquals(1, orderResponse.data().items().size());
        assertEquals("Laptop", orderResponse.data().items().get(0).productName());
    }

    @Test
    void shouldReturnAllOrdersSuccessfully() throws Exception {
        String customer1 = "123";
        String customer2 = "456";

        OrderItemRequest item1 = new OrderItemRequest("Laptop", 1, 1, new BigDecimal("5000.00"));
        OrderItemRequest item2 = new OrderItemRequest("Mouse", 2, 2, new BigDecimal("500.00"));

        CreateOrderRequest request1 = new CreateOrderRequest(List.of(item1));
        CreateOrderRequest request2 = new CreateOrderRequest(List.of(item2));

        mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customer1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customer2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        MvcResult getResult = mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All orders retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andReturn();

        ApiResponse<List<OrderResponse>> ordersResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<List<OrderResponse>>>() {}
        );

        assertEquals(2, ordersResponse.data().size());
    }

    @Test
    void shouldCancelOrderSuccessfully() throws Exception {
        String customerId = "123";
        OrderItemRequest item = new OrderItemRequest("Laptop", 1, 1, new BigDecimal("5000.00"));
        CreateOrderRequest request = new CreateOrderRequest(List.of(item));

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .header("X-Customer-Id", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        ApiResponse<CreateOrderResponse> createdOrder = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<CreateOrderResponse>>() {}
        );

        Long orderId = createdOrder.data().orderId();

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", orderId)
                        .header("X-Customer-Id", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        org.example.orderservice.entities.Order orderEntity = orderRepository.findById(orderId).get();
        assertEquals(OrderStatus.CANCELLED, orderEntity.getStatus());
    }
}
