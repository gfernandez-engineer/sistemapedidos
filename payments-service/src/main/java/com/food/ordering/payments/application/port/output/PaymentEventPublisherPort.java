package com.food.ordering.payments.application.port.output;

import com.food.ordering.payments.domain.model.Payment;

public interface PaymentEventPublisherPort {

    void publishPaymentCompleted(Payment payment);

    void publishPaymentFailed(Payment payment);
}
