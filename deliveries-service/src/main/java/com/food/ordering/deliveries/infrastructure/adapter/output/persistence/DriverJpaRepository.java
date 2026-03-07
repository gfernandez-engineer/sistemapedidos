package com.food.ordering.deliveries.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverJpaRepository extends JpaRepository<DriverEntity, Long> {

    List<DriverEntity> findByAvailableTrue();

    Optional<DriverEntity> findFirstByAvailableTrue();
}
