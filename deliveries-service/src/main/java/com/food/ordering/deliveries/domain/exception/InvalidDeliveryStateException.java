package com.food.ordering.deliveries.domain.exception;

public class InvalidDeliveryStateException extends RuntimeException {

    public InvalidDeliveryStateException(String currentStatus, String targetStatus) {
        super("Cannot transition delivery from " + currentStatus + " to " + targetStatus);
    }
}
