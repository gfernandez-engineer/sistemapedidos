package com.food.ordering.payments.application.port.input;

import com.food.ordering.payments.application.port.input.dto.PaymentResponse;
import com.food.ordering.payments.application.port.input.dto.ProcessPaymentCommand;

public interface ProcessPaymentUseCase {

    PaymentResponse process(ProcessPaymentCommand command);
}
