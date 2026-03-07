package com.food.ordering.users.infrastructure.adapter.input.rest.dto;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String address,
        String role,
        boolean active
) {
}
