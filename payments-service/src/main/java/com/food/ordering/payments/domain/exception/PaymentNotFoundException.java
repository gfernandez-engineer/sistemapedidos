package com.food.ordering.payments.domain.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }

    public PaymentNotFoundException(String orderId) {
        super("Payment not found for order: " + orderId);
    }
}
