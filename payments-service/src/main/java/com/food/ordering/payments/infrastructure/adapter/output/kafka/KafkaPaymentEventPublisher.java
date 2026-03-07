package com.food.ordering.payments.infrastructure.adapter.output.kafka;

import com.food.ordering.payments.application.port.output.PaymentEventPublisherPort;
import com.food.ordering.payments.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaPaymentEventPublisher.class);

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPaymentCompleted(Payment payment) {
        log.info("Publishing payment completed event for order: {}", payment.getOrderId());
        Map<String, Object> event = buildEvent(payment);
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, payment.getOrderId(), event);
    }

    @Override
    public void publishPaymentFailed(Payment payment) {
        log.info("Publishing payment failed event for order: {}", payment.getOrderId());
        Map<String, Object> event = buildEvent(payment);
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, payment.getOrderId(), event);
    }

    private Map<String, Object> buildEvent(Payment payment) {
        return Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrderId(),
                "userId", payment.getUserId(),
                "amount", payment.getAmount(),
                "paymentMethod", payment.getPaymentMethod().name(),
                "status", payment.getStatus().name(),
                "transactionId", payment.getTransactionId() != null ? payment.getTransactionId() : "",
                "createdAt", payment.getCreatedAt().toString()
        );
    }
}
