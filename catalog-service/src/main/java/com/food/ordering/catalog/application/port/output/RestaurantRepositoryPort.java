package com.food.ordering.catalog.application.port.output;

import com.food.ordering.catalog.domain.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepositoryPort {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(Long id);

    Page<Restaurant> findAll(Pageable pageable);

    List<Restaurant> findByCuisineType(String cuisineType);
}
