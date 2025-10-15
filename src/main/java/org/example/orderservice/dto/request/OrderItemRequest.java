package org.example.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;


@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "Details of a single order item")
public record OrderItemRequest(
        @NotNull(message = "Product name is required")
        @NotEmpty(message = "Product name is required")
        @Schema(description = "Product name", example = "Laptop", required = true)
        String productName,

        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID should be greater than zero")
        @Schema(description = "Product ID", example = "101", required = true)
        Integer productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity should be greater than zero")
        @Schema(description = "Quantity of the product", example = "2", required = true)
        Integer productQuantity,

        @NotNull(message = "Product price is required")
        @Positive(message = "Product price should be greater than zero")
        @Schema(description = "Price per product unit", example = "49999.99", required = true)
        BigDecimal productPrice

) { }
