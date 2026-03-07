package com.food.ordering.users.application.port.input;

import com.food.ordering.users.application.port.input.command.UpdateUserCommand;
import com.food.ordering.users.application.port.input.response.UserResponse;

public interface UpdateUserUseCase {

    UserResponse update(Long id, UpdateUserCommand command);
}
