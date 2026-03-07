package com.food.ordering.deliveries.infrastructure.adapter.input.rest;

import com.food.ordering.deliveries.application.port.input.ManageDriverUseCase;
import com.food.ordering.deliveries.application.port.input.RegisterDriverCommand;
import com.food.ordering.deliveries.domain.model.Driver;
import com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto.RegisterDriverRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final ManageDriverUseCase manageDriverUseCase;

    public DriverController(ManageDriverUseCase manageDriverUseCase) {
        this.manageDriverUseCase = manageDriverUseCase;
    }

    @PostMapping
    public ResponseEntity<Driver> registerDriver(@Valid @RequestBody RegisterDriverRequest request) {
        RegisterDriverCommand command = new RegisterDriverCommand(request.name(), request.phone(), request.vehicleType());
        Driver driver = manageDriverUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        List<Driver> drivers = manageDriverUseCase.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> toggleAvailability(@PathVariable Long id) {
        Driver driver = manageDriverUseCase.toggleAvailability(id);
        return ResponseEntity.ok(driver);
    }
}
