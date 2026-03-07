package com.food.ordering.orders.infrastructure.adapter.output.kafka;

import com.food.ordering.orders.application.port.output.OrderEventPublisherPort;
import com.food.ordering.orders.domain.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_STATUS_CHANGED_TOPIC = "order.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderCreated(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_CREATED");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("restaurantId", order.getRestaurantId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus().name());
        event.put("deliveryAddress", order.getDeliveryAddress());
        event.put("createdAt", order.getCreatedAt().toString());

        log.info("Publishing ORDER_CREATED event for order: {}", order.getId());
        kafkaTemplate.send(ORDER_CREATED_TOPIC, order.getId().toString(), event);
    }

    @Override
    public void publishOrderStatusChanged(Order order, String previousStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_STATUS_CHANGED");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("restaurantId", order.getRestaurantId());
        event.put("previousStatus", previousStatus);
        event.put("currentStatus", order.getStatus().name());
        event.put("updatedAt", order.getUpdatedAt().toString());

        log.info("Publishing ORDER_STATUS_CHANGED event for order: {} ({} -> {})",
                order.getId(), previousStatus, order.getStatus().name());
        kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, order.getId().toString(), event);
    }
}
