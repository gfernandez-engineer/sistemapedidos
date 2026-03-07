package com.food.ordering.orders.infrastructure.adapter.input.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        Long restaurantId,
        List<OrderItemResponseDto> items,
        String status,
        BigDecimal totalAmount,
        String deliveryAddress,
        String notes,
        LocalDateTime createdAt
) {

    public record OrderItemResponseDto(
            Long id,
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
