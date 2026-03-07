package com.food.ordering.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        String restaurantId,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        String deliveryAddress,
        Instant createdAt
) {
    public record OrderItemEvent(String productId, String productName, int quantity, BigDecimal unitPrice) {}
}
