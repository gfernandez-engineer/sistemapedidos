package com.food.ordering.deliveries.application.port.input;

public record CreateDeliveryCommand(String orderId, String deliveryAddress) {
}
