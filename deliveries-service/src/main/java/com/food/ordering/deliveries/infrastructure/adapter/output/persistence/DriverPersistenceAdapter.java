package com.food.ordering.deliveries.infrastructure.adapter.output.persistence;

import com.food.ordering.deliveries.application.port.output.DriverRepositoryPort;
import com.food.ordering.deliveries.domain.model.Driver;
import com.food.ordering.deliveries.infrastructure.adapter.output.mapper.DriverMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DriverPersistenceAdapter implements DriverRepositoryPort {

    private final DriverJpaRepository jpaRepository;
    private final DriverMapper mapper;

    public DriverPersistenceAdapter(DriverJpaRepository jpaRepository, DriverMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Driver save(Driver driver) {
        DriverEntity entity = mapper.toEntity(driver);
        DriverEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Driver> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return jpaRepository.findByAvailableTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Driver> findFirstAvailable() {
        return jpaRepository.findFirstByAvailableTrue().map(mapper::toDomain);
    }
}
