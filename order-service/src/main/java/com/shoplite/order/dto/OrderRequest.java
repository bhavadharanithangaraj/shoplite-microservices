package com.shoplite.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// JSON body for POST /orders, e.g. { "productId": 1, "quantity": 2 }
public record OrderRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity
) {
}
