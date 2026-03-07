package com.food.ordering.catalog.infrastructure.adapter.output.persistence;

import com.food.ordering.catalog.application.port.output.ProductRepositoryPort;
import com.food.ordering.catalog.domain.model.Product;
import com.food.ordering.catalog.infrastructure.adapter.output.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;

    public ProductPersistenceAdapter(ProductJpaRepository productJpaRepository,
                                    ProductMapper productMapper) {
        this.productJpaRepository = productJpaRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        ProductEntity saved = productJpaRepository.save(entity);
        return productMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findByRestaurantId(Long restaurantId) {
        return productJpaRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }
}
