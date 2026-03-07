package com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDriverRequest(
        @NotBlank(message = "Driver name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Vehicle type is required")
        String vehicleType
) {
}
