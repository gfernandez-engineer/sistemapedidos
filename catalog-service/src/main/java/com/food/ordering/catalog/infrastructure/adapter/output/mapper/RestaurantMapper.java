package com.food.ordering.catalog.infrastructure.adapter.output.mapper;

import com.food.ordering.catalog.domain.model.Restaurant;
import com.food.ordering.catalog.infrastructure.adapter.output.persistence.RestaurantEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    RestaurantEntity toEntity(Restaurant restaurant);

    Restaurant toDomain(RestaurantEntity entity);
}
