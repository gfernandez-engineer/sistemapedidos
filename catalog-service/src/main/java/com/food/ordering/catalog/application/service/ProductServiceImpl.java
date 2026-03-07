package com.food.ordering.catalog.application.service;

import com.food.ordering.catalog.application.port.input.ManageProductUseCase;
import com.food.ordering.catalog.application.port.output.ProductRepositoryPort;
import com.food.ordering.catalog.application.port.output.RestaurantRepositoryPort;
import com.food.ordering.catalog.domain.exception.ProductNotFoundException;
import com.food.ordering.catalog.domain.exception.RestaurantNotFoundException;
import com.food.ordering.catalog.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ManageProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ProductServiceImpl(ProductRepositoryPort productRepositoryPort,
                              RestaurantRepositoryPort restaurantRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public ProductResponse create(CreateProductCommand command) {
        restaurantRepositoryPort.findById(command.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(command.restaurantId()));

        Product product = new Product();
        product.setRestaurantId(command.restaurantId());
        product.setName(command.name());
        product.setDescription(command.description());
        product.setPrice(command.price());
        product.setCategory(command.category());
        product.setImageUrl(command.imageUrl());
        product.setAvailable(true);

        Product saved = productRepositoryPort.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByRestaurantId(Long restaurantId) {
        return productRepositoryPort.findByRestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse update(Long id, UpdateProductCommand command) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(command.name());
        product.setDescription(command.description());
        product.setPrice(command.price());
        product.setCategory(command.category());
        product.setImageUrl(command.imageUrl());

        Product updated = productRepositoryPort.save(product);
        return toResponse(updated);
    }

    @Override
    public ProductResponse toggleAvailability(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setAvailable(!product.isAvailable());

        Product updated = productRepositoryPort.save(product);
        return toResponse(updated);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getRestaurantId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isAvailable()
        );
    }
}
