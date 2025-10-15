package org.example.orderservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
@Schema(description = "Response details of a single order item")
public record OrderItemResponse(
        @Schema(description = "Product name", example = "Laptop")
        String productName,

        @Schema(description = "Product price", example = "49999.99")
        BigDecimal productPrice,

        @Schema(description = "Quantity of product", example = "2")
        int productQuantity,

        @Schema(description = "Product ID", example = "101")
        int productId
        ) {
}
