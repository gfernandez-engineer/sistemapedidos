package com.food.ordering.orders.infrastructure.adapter.output.persistence;

import com.food.ordering.orders.application.port.output.OrderRepositoryPort;
import com.food.ordering.orders.domain.model.Order;
import com.food.ordering.orders.infrastructure.adapter.output.mapper.OrderMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper orderMapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderMapper orderMapper) {
        this.jpaRepository = jpaRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        entity.getItems().forEach(item -> item.setOrder(entity));
        OrderEntity savedEntity = jpaRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(orderMapper::toDomain)
                .toList();
    }
}
