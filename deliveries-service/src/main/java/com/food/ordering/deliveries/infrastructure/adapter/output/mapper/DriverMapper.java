package com.food.ordering.deliveries.infrastructure.adapter.output.mapper;

import com.food.ordering.deliveries.domain.model.Driver;
import com.food.ordering.deliveries.infrastructure.adapter.output.persistence.DriverEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverEntity toEntity(Driver driver);

    Driver toDomain(DriverEntity entity);
}
