package com.food.ordering.deliveries.domain.exception;

public class NoAvailableDriverException extends RuntimeException {

    public NoAvailableDriverException() {
        super("No available drivers at the moment");
    }
}
