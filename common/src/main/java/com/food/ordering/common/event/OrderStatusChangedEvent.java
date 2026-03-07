package com.food.ordering.common.event;

import java.time.Instant;

public record OrderStatusChangedEvent(
        String orderId,
        String previousStatus,
        String newStatus,
        Instant changedAt
) {}
