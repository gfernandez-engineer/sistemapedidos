package com.food.ordering.payments.application.port.input.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String orderId,
        String userId,
        BigDecimal amount,
        String paymentMethod,
        String status,
        String transactionId,
        LocalDateTime createdAt
) {
}
