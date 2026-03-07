package com.food.ordering.catalog.domain.exception;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long id) {
        super("Restaurant not found with id: " + id);
    }

    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
