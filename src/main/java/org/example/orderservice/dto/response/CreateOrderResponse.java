package org.example.orderservice.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after successfully creating an order")
public record CreateOrderResponse(
        @Schema(description = "Generated order ID", example = "12345")
        Long orderId
) {
}
