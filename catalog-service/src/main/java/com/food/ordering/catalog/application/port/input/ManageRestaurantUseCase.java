package com.food.ordering.catalog.application.port.input;

import com.food.ordering.catalog.domain.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ManageRestaurantUseCase {

    RestaurantResponse create(CreateRestaurantCommand command);

    RestaurantResponse getById(Long id);

    Page<RestaurantResponse> getAll(Pageable pageable);

    List<RestaurantResponse> searchByCuisineType(String cuisineType);

    record CreateRestaurantCommand(
            String name,
            String description,
            String address,
            String phone,
            String cuisineType
    ) {}

    record RestaurantResponse(
            Long id,
            String name,
            String description,
            String address,
            String phone,
            String cuisineType,
            BigDecimal rating,
            boolean active
    ) {}
}
