package com.food.ordering.orders.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserId(Long userId);
}
