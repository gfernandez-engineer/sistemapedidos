package com.food.ordering.orders.application.port.input.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        Long restaurantId,
        List<OrderItemResponse> items,
        String status,
        BigDecimal totalAmount,
        String deliveryAddress,
        String notes,
        LocalDateTime createdAt
) {
}
