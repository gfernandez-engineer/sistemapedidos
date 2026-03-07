package com.food.ordering.orders.application.port.input.command;

import java.math.BigDecimal;

public record OrderItemCommand(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
