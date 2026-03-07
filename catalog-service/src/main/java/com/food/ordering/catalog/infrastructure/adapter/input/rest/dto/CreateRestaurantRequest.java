package com.food.ordering.catalog.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRestaurantRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Cuisine type is required")
        String cuisineType
) {}
