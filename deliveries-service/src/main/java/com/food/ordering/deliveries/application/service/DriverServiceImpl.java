package com.food.ordering.deliveries.application.service;

import com.food.ordering.deliveries.application.port.input.ManageDriverUseCase;
import com.food.ordering.deliveries.application.port.input.RegisterDriverCommand;
import com.food.ordering.deliveries.application.port.output.DriverRepositoryPort;
import com.food.ordering.deliveries.domain.model.Driver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DriverServiceImpl implements ManageDriverUseCase {

    private final DriverRepositoryPort driverRepository;

    public DriverServiceImpl(DriverRepositoryPort driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public Driver register(RegisterDriverCommand command) {
        Driver driver = new Driver();
        driver.setName(command.name());
        driver.setPhone(command.phone());
        driver.setVehicleType(command.vehicleType());
        driver.setAvailable(true);
        return driverRepository.save(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers();
    }

    @Override
    public Driver toggleAvailability(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
        driver.setAvailable(!driver.isAvailable());
        return driverRepository.save(driver);
    }
}
