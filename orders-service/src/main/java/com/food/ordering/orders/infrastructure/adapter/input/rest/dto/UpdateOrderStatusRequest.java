package com.food.ordering.orders.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {
}
