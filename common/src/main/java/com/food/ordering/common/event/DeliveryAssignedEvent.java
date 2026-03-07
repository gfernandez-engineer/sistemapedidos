package com.food.ordering.common.event;

import java.time.Instant;

public record DeliveryAssignedEvent(
        String deliveryId,
        String orderId,
        String driverId,
        String deliveryAddress,
        Instant assignedAt
) {}
