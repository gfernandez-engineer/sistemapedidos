package com.food.ordering.orders.application.port.input.command;

import java.util.List;

public record CreateOrderCommand(
        Long userId,
        Long restaurantId,
        List<OrderItemCommand> items,
        String deliveryAddress,
        String notes
) {
}
