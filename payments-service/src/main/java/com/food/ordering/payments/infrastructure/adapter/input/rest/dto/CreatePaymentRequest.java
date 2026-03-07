package com.food.ordering.payments.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Payment method is required")
        String paymentMethod
) {
}
