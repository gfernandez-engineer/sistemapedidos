package com.food.ordering.catalog.infrastructure.adapter.input.rest;

import com.food.ordering.catalog.application.port.input.ManageProductUseCase;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase.CreateProductCommand;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase.ProductResponse;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase.UpdateProductCommand;
import com.food.ordering.catalog.infrastructure.adapter.input.rest.dto.CreateProductRequest;
import com.food.ordering.catalog.infrastructure.adapter.input.rest.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ManageProductUseCase manageProductUseCase;

    public ProductController(ManageProductUseCase manageProductUseCase) {
        this.manageProductUseCase = manageProductUseCase;
    }

    @PostMapping("/restaurants/{restaurantId}/products")
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                restaurantId,
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.imageUrl()
        );
        ProductResponse response = manageProductUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        ProductResponse response = manageProductUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}/products")
    public ResponseEntity<List<ProductResponse>> getByRestaurantId(
            @PathVariable Long restaurantId) {
        List<ProductResponse> response = manageProductUseCase.getByRestaurantId(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        UpdateProductCommand command = new UpdateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.imageUrl()
        );
        ProductResponse response = manageProductUseCase.update(id, command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/products/{id}/availability")
    public ResponseEntity<ProductResponse> toggleAvailability(@PathVariable Long id) {
        ProductResponse response = manageProductUseCase.toggleAvailability(id);
        return ResponseEntity.ok(response);
    }
}
