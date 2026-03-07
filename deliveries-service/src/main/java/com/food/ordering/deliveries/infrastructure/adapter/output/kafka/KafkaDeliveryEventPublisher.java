package com.food.ordering.deliveries.infrastructure.adapter.output.kafka;

import com.food.ordering.deliveries.application.port.output.DeliveryEventPublisherPort;
import com.food.ordering.deliveries.domain.model.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaDeliveryEventPublisher implements DeliveryEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaDeliveryEventPublisher.class);

    private static final String DELIVERY_ASSIGNED_TOPIC = "delivery.assigned";
    private static final String DELIVERY_STATUS_CHANGED_TOPIC = "delivery.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaDeliveryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishDeliveryAssigned(Delivery delivery) {
        Map<String, Object> event = buildEvent(delivery);
        event.put("eventType", "DELIVERY_ASSIGNED");
        kafkaTemplate.send(DELIVERY_ASSIGNED_TOPIC, delivery.getOrderId(), event);
        log.info("Published delivery assigned event for order: {}", delivery.getOrderId());
    }

    @Override
    public void publishDeliveryStatusChanged(Delivery delivery) {
        Map<String, Object> event = buildEvent(delivery);
        event.put("eventType", "DELIVERY_STATUS_CHANGED");
        kafkaTemplate.send(DELIVERY_STATUS_CHANGED_TOPIC, delivery.getOrderId(), event);
        log.info("Published delivery status changed event for order: {}, status: {}",
                delivery.getOrderId(), delivery.getStatus().name());
    }

    private Map<String, Object> buildEvent(Delivery delivery) {
        Map<String, Object> event = new HashMap<>();
        event.put("deliveryId", delivery.getId());
        event.put("orderId", delivery.getOrderId());
        event.put("driverId", delivery.getDriverId());
        event.put("status", delivery.getStatus().name());
        event.put("deliveryAddress", delivery.getDeliveryAddress());
        event.put("estimatedDeliveryTime", delivery.getEstimatedDeliveryTime() != null
                ? delivery.getEstimatedDeliveryTime().toString() : null);
        return event;
    }
}
