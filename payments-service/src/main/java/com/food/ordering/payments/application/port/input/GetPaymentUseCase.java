package com.food.ordering.payments.application.port.input;

import com.food.ordering.payments.application.port.input.dto.PaymentResponse;

public interface GetPaymentUseCase {

    PaymentResponse getById(Long id);

    PaymentResponse getByOrderId(String orderId);
}
