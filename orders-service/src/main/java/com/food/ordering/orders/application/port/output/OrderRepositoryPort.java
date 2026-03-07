package com.food.ordering.orders.application.port.output;

import com.food.ordering.orders.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findByUserId(Long userId);
}
