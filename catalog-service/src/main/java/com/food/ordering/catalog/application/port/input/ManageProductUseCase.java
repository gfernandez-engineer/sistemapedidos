package com.food.ordering.catalog.application.port.input;

import java.math.BigDecimal;
import java.util.List;

public interface ManageProductUseCase {

    ProductResponse create(CreateProductCommand command);

    ProductResponse getById(Long id);

    List<ProductResponse> getByRestaurantId(Long restaurantId);

    ProductResponse update(Long id, UpdateProductCommand command);

    ProductResponse toggleAvailability(Long id);

    record CreateProductCommand(
            Long restaurantId,
            String name,
            String description,
            BigDecimal price,
            String category,
            String imageUrl
    ) {}

    record UpdateProductCommand(
            String name,
            String description,
            BigDecimal price,
            String category,
            String imageUrl
    ) {}

    record ProductResponse(
            Long id,
            Long restaurantId,
            String name,
            String description,
            BigDecimal price,
            String category,
            String imageUrl,
            boolean available
    ) {}
}
