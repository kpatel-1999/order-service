package org.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
        @Schema(description = "Response message", example = "Order placed successfully")
        String message,

        @Schema(description = "Response payload")
        T data
) {}
