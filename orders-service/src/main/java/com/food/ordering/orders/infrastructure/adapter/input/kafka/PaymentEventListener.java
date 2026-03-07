package com.food.ordering.orders.infrastructure.adapter.input.kafka;

import com.food.ordering.orders.application.port.input.UpdateOrderStatusUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public PaymentEventListener(UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    @KafkaListener(topics = "payment.completed", groupId = "orders-service-group")
    public void handlePaymentCompleted(Map<String, Object> event) {
        Long orderId = extractOrderId(event);
        log.info("Payment completed event received for order: {}", orderId);
        try {
            updateOrderStatusUseCase.updateStatus(orderId, "CONFIRMED");
        } catch (Exception e) {
            log.error("Error processing payment completed event for order {}: {}", orderId, e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "orders-service-group")
    public void handlePaymentFailed(Map<String, Object> event) {
        Long orderId = extractOrderId(event);
        log.info("Payment failed event received for order: {}", orderId);
        try {
            updateOrderStatusUseCase.updateStatus(orderId, "CANCELLED");
        } catch (Exception e) {
            log.error("Error processing payment failed event for order {}: {}", orderId, e.getMessage());
        }
    }

    private Long extractOrderId(Map<String, Object> event) {
        Object orderIdObj = event.get("orderId");
        if (orderIdObj instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(orderIdObj.toString());
    }
}
