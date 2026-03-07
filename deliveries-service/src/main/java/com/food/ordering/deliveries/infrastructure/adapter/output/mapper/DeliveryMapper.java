package com.food.ordering.deliveries.infrastructure.adapter.output.mapper;

import com.food.ordering.deliveries.domain.model.Delivery;
import com.food.ordering.deliveries.domain.model.DeliveryStatus;
import com.food.ordering.deliveries.infrastructure.adapter.output.persistence.DeliveryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    DeliveryEntity toEntity(Delivery delivery);

    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Delivery toDomain(DeliveryEntity entity);

    @Named("statusToString")
    default String statusToString(DeliveryStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default DeliveryStatus stringToStatus(String status) {
        return status != null ? DeliveryStatus.fromString(status) : null;
    }
}
