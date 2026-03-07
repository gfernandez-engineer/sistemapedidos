package com.food.ordering.catalog.infrastructure.adapter.input.rest;

import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase;
import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase.CreateRestaurantCommand;
import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase.RestaurantResponse;
import com.food.ordering.catalog.infrastructure.adapter.input.rest.dto.CreateRestaurantRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final ManageRestaurantUseCase manageRestaurantUseCase;

    public RestaurantController(ManageRestaurantUseCase manageRestaurantUseCase) {
        this.manageRestaurantUseCase = manageRestaurantUseCase;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody CreateRestaurantRequest request) {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                request.name(),
                request.description(),
                request.address(),
                request.phone(),
                request.cuisineType()
        );
        RestaurantResponse response = manageRestaurantUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        RestaurantResponse response = manageRestaurantUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getAll(Pageable pageable) {
        Page<RestaurantResponse> response = manageRestaurantUseCase.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchByCuisineType(
            @RequestParam String cuisineType) {
        List<RestaurantResponse> response = manageRestaurantUseCase.searchByCuisineType(cuisineType);
        return ResponseEntity.ok(response);
    }
}
