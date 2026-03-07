package com.food.ordering.deliveries.application.port.output;

import com.food.ordering.deliveries.domain.model.Delivery;

import java.util.Optional;

public interface DeliveryRepositoryPort {

    Delivery save(Delivery delivery);

    Optional<Delivery> findById(Long id);

    Optional<Delivery> findByOrderId(String orderId);
}
