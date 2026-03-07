package com.food.ordering.deliveries.domain.exception;

public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(Long id) {
        super("Delivery not found with id: " + id);
    }

    public DeliveryNotFoundException(String orderId) {
        super("Delivery not found for order: " + orderId);
    }
}
