package com.food.ordering.users.infrastructure.adapter.input.rest.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String phone,
        String address
) {
}
