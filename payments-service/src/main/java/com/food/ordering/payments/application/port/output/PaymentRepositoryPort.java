package com.food.ordering.payments.application.port.output;

import com.food.ordering.payments.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderId(String orderId);
}
