package com.food.ordering.orders.application.port.output;

import com.food.ordering.orders.domain.model.Order;

public interface OrderEventPublisherPort {

    void publishOrderCreated(Order order);

    void publishOrderStatusChanged(Order order, String previousStatus);
}
