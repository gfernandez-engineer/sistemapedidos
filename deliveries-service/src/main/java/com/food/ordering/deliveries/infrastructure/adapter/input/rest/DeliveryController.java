package com.food.ordering.deliveries.infrastructure.adapter.input.rest;

import com.food.ordering.deliveries.application.port.input.CreateDeliveryCommand;
import com.food.ordering.deliveries.application.port.input.DeliveryResponse;
import com.food.ordering.deliveries.application.port.input.ManageDeliveryUseCase;
import com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto.CreateDeliveryRequest;
import com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final ManageDeliveryUseCase manageDeliveryUseCase;

    public DeliveryController(ManageDeliveryUseCase manageDeliveryUseCase) {
        this.manageDeliveryUseCase = manageDeliveryUseCase;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        CreateDeliveryCommand command = new CreateDeliveryCommand(request.orderId(), request.deliveryAddress());
        DeliveryResponse response = manageDeliveryUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        DeliveryResponse response = manageDeliveryUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable String orderId) {
        DeliveryResponse response = manageDeliveryUseCase.getByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateStatusRequest request) {
        DeliveryResponse response = manageDeliveryUseCase.updateStatus(id, request.status());
        return ResponseEntity.ok(response);
    }
}
