package com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeliveryRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress
) {
}
