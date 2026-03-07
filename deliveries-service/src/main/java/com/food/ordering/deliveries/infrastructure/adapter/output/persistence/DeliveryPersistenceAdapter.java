package com.food.ordering.deliveries.infrastructure.adapter.output.persistence;

import com.food.ordering.deliveries.application.port.output.DeliveryRepositoryPort;
import com.food.ordering.deliveries.domain.model.Delivery;
import com.food.ordering.deliveries.infrastructure.adapter.output.mapper.DeliveryMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeliveryPersistenceAdapter implements DeliveryRepositoryPort {

    private final DeliveryJpaRepository jpaRepository;
    private final DeliveryMapper mapper;

    public DeliveryPersistenceAdapter(DeliveryJpaRepository jpaRepository, DeliveryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Delivery save(Delivery delivery) {
        DeliveryEntity entity = mapper.toEntity(delivery);
        DeliveryEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Delivery> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDomain);
    }
}
