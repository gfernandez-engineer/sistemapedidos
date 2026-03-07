package com.food.ordering.users.application.port.input.command;

public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        String address,
        String role
) {
}
