package com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {
}
