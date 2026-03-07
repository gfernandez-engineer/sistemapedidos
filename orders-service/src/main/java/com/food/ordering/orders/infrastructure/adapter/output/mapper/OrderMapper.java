package com.food.ordering.orders.infrastructure.adapter.output.mapper;

import com.food.ordering.orders.domain.model.Order;
import com.food.ordering.orders.domain.model.OrderItem;
import com.food.ordering.orders.domain.model.OrderStatus;
import com.food.ordering.orders.infrastructure.adapter.output.persistence.OrderEntity;
import com.food.ordering.orders.infrastructure.adapter.output.persistence.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderEntity toEntity(Order order);

    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Order toDomain(OrderEntity entity);

    OrderItemEntity toItemEntity(OrderItem item);

    OrderItem toItemDomain(OrderItemEntity entity);

    List<OrderItemEntity> toItemEntities(List<OrderItem> items);

    List<OrderItem> toItemDomains(List<OrderItemEntity> entities);

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default OrderStatus stringToStatus(String status) {
        return status != null ? OrderStatus.fromString(status) : null;
    }
}
