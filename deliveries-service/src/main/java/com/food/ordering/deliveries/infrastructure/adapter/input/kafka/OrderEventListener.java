package com.food.ordering.deliveries.infrastructure.adapter.input.kafka;

import com.food.ordering.deliveries.application.port.input.CreateDeliveryCommand;
import com.food.ordering.deliveries.application.port.input.ManageDeliveryUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final ManageDeliveryUseCase manageDeliveryUseCase;

    public OrderEventListener(ManageDeliveryUseCase manageDeliveryUseCase) {
        this.manageDeliveryUseCase = manageDeliveryUseCase;
    }

    @KafkaListener(topics = "order.status.changed", groupId = "deliveries-service-group")
    public void handleOrderStatusChanged(Map<String, Object> event) {
        try {
            String status = (String) event.get("status");
            String orderId = (String) event.get("orderId");
            String deliveryAddress = (String) event.get("deliveryAddress");

            log.info("Received order status changed event: orderId={}, status={}", orderId, status);

            if ("READY".equalsIgnoreCase(status)) {
                CreateDeliveryCommand command = new CreateDeliveryCommand(orderId, deliveryAddress);
                manageDeliveryUseCase.create(command);
                log.info("Delivery created automatically for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Error processing order status changed event: {}", e.getMessage(), e);
        }
    }
}
