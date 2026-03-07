package com.food.ordering.users.infrastructure.adapter.output.mapper;

import com.food.ordering.users.domain.model.User;
import com.food.ordering.users.domain.model.User.UserRole;
import com.food.ordering.users.infrastructure.adapter.output.persistence.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    UserEntity toEntity(User user);

    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRole")
    User toDomain(UserEntity entity);

    @Named("roleToString")
    default String roleToString(UserRole role) {
        if (role == null) return null;
        return role.name();
    }

    @Named("stringToRole")
    default UserRole stringToRole(String role) {
        if (role == null) return null;
        return switch (role) {
            case "CUSTOMER" -> new UserRole.Customer();
            case "RESTAURANT_OWNER" -> new UserRole.RestaurantOwner();
            case "DELIVERY_DRIVER" -> new UserRole.DeliveryDriver();
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
