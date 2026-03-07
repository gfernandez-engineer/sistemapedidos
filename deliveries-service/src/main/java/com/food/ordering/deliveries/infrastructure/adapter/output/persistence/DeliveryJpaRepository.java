package com.food.ordering.deliveries.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryJpaRepository extends JpaRepository<DeliveryEntity, Long> {

    Optional<DeliveryEntity> findByOrderId(String orderId);
}
