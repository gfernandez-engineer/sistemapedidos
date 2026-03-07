package com.food.ordering.payments.application.port.input.dto;

import com.food.ordering.payments.domain.model.PaymentMethod;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentMethod paymentMethod
) {
}
