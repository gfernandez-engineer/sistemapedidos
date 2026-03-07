package com.food.ordering.deliveries.application.port.output;

import com.food.ordering.deliveries.domain.model.Delivery;

public interface DeliveryEventPublisherPort {

    void publishDeliveryAssigned(Delivery delivery);

    void publishDeliveryStatusChanged(Delivery delivery);
}
