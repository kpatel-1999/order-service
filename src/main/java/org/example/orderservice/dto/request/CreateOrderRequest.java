package org.example.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "Request payload to create a new order")
public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        @Schema(description = "List of items in the order", required = true)
        List<OrderItemRequest> items

) {
}
