package com.food.ordering.orders.domain.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }
}
