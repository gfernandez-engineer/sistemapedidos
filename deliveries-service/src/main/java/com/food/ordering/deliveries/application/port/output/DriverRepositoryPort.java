package com.food.ordering.deliveries.application.port.output;

import com.food.ordering.deliveries.domain.model.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverRepositoryPort {

    Driver save(Driver driver);

    Optional<Driver> findById(Long id);

    List<Driver> findAvailableDrivers();

    Optional<Driver> findFirstAvailable();
}
