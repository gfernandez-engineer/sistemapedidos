package com.food.ordering.orders.application.port.input;

import com.food.ordering.orders.application.port.input.command.CreateOrderCommand;
import com.food.ordering.orders.application.port.input.response.OrderResponse;

public interface CreateOrderUseCase {

    OrderResponse create(CreateOrderCommand command);
}
