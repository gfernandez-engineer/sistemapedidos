package com.food.ordering.deliveries.application.port.input;

import java.time.Instant;

public record DeliveryResponse(
        Long id,
        String orderId,
        Long driverId,
        String deliveryAddress,
        String status,
        Instant estimatedDeliveryTime,
        Instant actualDeliveryTime,
        Instant createdAt
) {
}
