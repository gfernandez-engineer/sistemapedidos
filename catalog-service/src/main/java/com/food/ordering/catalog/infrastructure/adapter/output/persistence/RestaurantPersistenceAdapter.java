package com.food.ordering.catalog.infrastructure.adapter.output.persistence;

import com.food.ordering.catalog.application.port.output.RestaurantRepositoryPort;
import com.food.ordering.catalog.domain.model.Restaurant;
import com.food.ordering.catalog.infrastructure.adapter.output.mapper.RestaurantMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RestaurantPersistenceAdapter implements RestaurantRepositoryPort {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantPersistenceAdapter(RestaurantJpaRepository restaurantJpaRepository,
                                       RestaurantMapper restaurantMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantEntity entity = restaurantMapper.toEntity(restaurant);
        RestaurantEntity saved = restaurantJpaRepository.save(entity);
        return restaurantMapper.toDomain(saved);
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantJpaRepository.findById(id)
                .map(restaurantMapper::toDomain);
    }

    @Override
    public Page<Restaurant> findAll(Pageable pageable) {
        return restaurantJpaRepository.findAll(pageable)
                .map(restaurantMapper::toDomain);
    }

    @Override
    public List<Restaurant> findByCuisineType(String cuisineType) {
        return restaurantJpaRepository.findByCuisineTypeIgnoreCase(cuisineType)
                .stream()
                .map(restaurantMapper::toDomain)
                .toList();
    }
}
