package com.food.ordering.orders.application.port.input.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
