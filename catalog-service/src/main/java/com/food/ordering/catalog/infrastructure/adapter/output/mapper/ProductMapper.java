package com.food.ordering.catalog.infrastructure.adapter.output.mapper;

import com.food.ordering.catalog.domain.model.Product;
import com.food.ordering.catalog.infrastructure.adapter.output.persistence.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductEntity toEntity(Product product);

    Product toDomain(ProductEntity entity);
}
