package com.food.ordering.orders.infrastructure.adapter.input.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Restaurant ID is required")
        Long restaurantId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress,

        String notes
) {
}
