package com.food.ordering.deliveries.application.port.input;

public record RegisterDriverCommand(String name, String phone, String vehicleType) {
}
