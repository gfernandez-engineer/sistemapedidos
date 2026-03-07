package com.food.ordering.payments.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(String orderId);
}
