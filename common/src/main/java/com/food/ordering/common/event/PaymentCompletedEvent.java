package com.food.ordering.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        String paymentId,
        String orderId,
        String userId,
        BigDecimal amount,
        String status,
        Instant processedAt
) {}
