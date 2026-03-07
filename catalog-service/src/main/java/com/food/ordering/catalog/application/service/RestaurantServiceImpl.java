package com.food.ordering.catalog.application.service;

import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase;
import com.food.ordering.catalog.application.port.output.RestaurantRepositoryPort;
import com.food.ordering.catalog.domain.exception.RestaurantNotFoundException;
import com.food.ordering.catalog.domain.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class RestaurantServiceImpl implements ManageRestaurantUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public RestaurantServiceImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public RestaurantResponse create(CreateRestaurantCommand command) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(command.name());
        restaurant.setDescription(command.description());
        restaurant.setAddress(command.address());
        restaurant.setPhone(command.phone());
        restaurant.setCuisineType(command.cuisineType());
        restaurant.setRating(BigDecimal.ZERO);
        restaurant.setActive(true);

        Restaurant saved = restaurantRepositoryPort.save(restaurant);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {
        Restaurant restaurant = restaurantRepositoryPort.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        return toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAll(Pageable pageable) {
        return restaurantRepositoryPort.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCuisineType(String cuisineType) {
        return restaurantRepositoryPort.findByCuisineType(cuisineType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getCuisineType(),
                restaurant.getRating(),
                restaurant.isActive()
        );
    }
}
