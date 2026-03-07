package com.food.ordering.users.application.port.input.response;

public record UserResponse(
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
