package org.example.orderservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.orderservice.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@Schema(description = "Response returned when fetching order details")
public record OrderResponse(
        @Schema(description = "Order ID", example = "12345")
        Long orderId,

        @Schema(description = "Customer ID", example = "cust-001")
        String customerId,

        @Schema(description = "Order status")
        OrderStatus status,

        @Schema(description = "Total order amount", example = "99999.99")
        BigDecimal totalAmount,

        @Schema(description = "Order creation timestamp")
        Instant createdAt,
        List<OrderItemResponse> items


) {
}
