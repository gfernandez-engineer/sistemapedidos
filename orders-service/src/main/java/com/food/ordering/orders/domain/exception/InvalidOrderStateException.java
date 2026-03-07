package com.food.ordering.orders.domain.exception;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String currentStatus, String targetStatus) {
        super("Cannot transition order from " + currentStatus + " to " + targetStatus);
    }
}
