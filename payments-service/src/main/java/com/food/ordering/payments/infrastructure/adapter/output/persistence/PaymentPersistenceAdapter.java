package com.food.ordering.payments.infrastructure.adapter.output.persistence;

import com.food.ordering.payments.application.port.output.PaymentRepositoryPort;
import com.food.ordering.payments.domain.model.Payment;
import com.food.ordering.payments.infrastructure.adapter.output.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    public PaymentPersistenceAdapter(PaymentJpaRepository jpaRepository, PaymentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = mapper.toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDomain);
    }
}
