package com.food.ordering.payments.infrastructure.adapter.input.kafka;

import com.food.ordering.payments.application.port.input.ProcessPaymentUseCase;
import com.food.ordering.payments.application.port.input.dto.ProcessPaymentCommand;
import com.food.ordering.payments.domain.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final ProcessPaymentUseCase processPaymentUseCase;

    public OrderEventListener(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @KafkaListener(topics = "order.created", groupId = "payments-service-group")
    public void handleOrderCreated(Map<String, Object> event) {
        log.info("Received order created event: {}", event);

        try {
            String orderId = (String) event.get("orderId");
            String userId = (String) event.get("userId");
            BigDecimal amount = event.get("totalAmount") instanceof BigDecimal bd
                    ? bd
                    : new BigDecimal(event.get("totalAmount").toString());
            String paymentMethodStr = (String) event.getOrDefault("paymentMethod", "CREDIT_CARD");
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);

            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    orderId, userId, amount, paymentMethod
            );

            processPaymentUseCase.process(command);
            log.info("Payment processed successfully for order: {}", orderId);

        } catch (Exception e) {
            log.error("Error processing order created event: {}", event, e);
        }
    }
}
