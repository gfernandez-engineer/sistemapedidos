package com.food.ordering.catalog.application.port.output;

import com.food.ordering.catalog.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findByRestaurantId(Long restaurantId);
}
