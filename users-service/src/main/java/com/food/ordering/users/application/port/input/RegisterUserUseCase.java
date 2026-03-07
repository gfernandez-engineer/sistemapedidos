package com.food.ordering.users.application.port.input;

import com.food.ordering.users.application.port.input.command.RegisterUserCommand;
import com.food.ordering.users.application.port.input.response.UserResponse;

public interface RegisterUserUseCase {

    UserResponse register(RegisterUserCommand command);
}
