package com.food.ordering.orders.application.port.input;

import com.food.ordering.orders.application.port.input.response.OrderResponse;

public interface UpdateOrderStatusUseCase {

    OrderResponse updateStatus(Long id, String status);
}
