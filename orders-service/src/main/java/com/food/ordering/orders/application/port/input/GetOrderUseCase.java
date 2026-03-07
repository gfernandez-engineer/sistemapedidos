package com.food.ordering.orders.application.port.input;

import com.food.ordering.orders.application.port.input.response.OrderResponse;

import java.util.List;

public interface GetOrderUseCase {

    OrderResponse getById(Long id);

    List<OrderResponse> getByUserId(Long userId);
}
