package com.food.ordering.deliveries.application.port.input;

import com.food.ordering.deliveries.domain.model.Driver;

import java.util.List;

public interface ManageDriverUseCase {

    Driver register(RegisterDriverCommand command);

    List<Driver> getAvailableDrivers();

    Driver toggleAvailability(Long id);
}
