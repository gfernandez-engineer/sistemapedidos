package com.food.ordering.users.application.port.input;

import com.food.ordering.users.application.port.input.response.UserResponse;

public interface GetUserUseCase {

    UserResponse getById(Long id);

    UserResponse getByEmail(String email);
}
