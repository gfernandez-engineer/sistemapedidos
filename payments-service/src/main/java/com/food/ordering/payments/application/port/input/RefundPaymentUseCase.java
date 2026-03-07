package com.food.ordering.payments.application.port.input;

import com.food.ordering.payments.application.port.input.dto.PaymentResponse;

public interface RefundPaymentUseCase {

    PaymentResponse refund(Long id);
}
