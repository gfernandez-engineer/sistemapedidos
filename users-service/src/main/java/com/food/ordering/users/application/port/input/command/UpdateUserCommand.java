package com.food.ordering.users.application.port.input.command;

public record UpdateUserCommand(
        String firstName,
        String lastName,
        String phone,
        String address
) {
}
