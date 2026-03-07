package com.food.ordering.users.infrastructure.adapter.input.rest;

import com.food.ordering.users.application.port.input.GetUserUseCase;
import com.food.ordering.users.application.port.input.RegisterUserUseCase;
import com.food.ordering.users.application.port.input.UpdateUserUseCase;
import com.food.ordering.users.application.port.input.command.RegisterUserCommand;
import com.food.ordering.users.application.port.input.command.UpdateUserCommand;
import com.food.ordering.users.application.port.input.response.UserResponse;
import com.food.ordering.users.infrastructure.adapter.input.rest.dto.RegisterUserRequest;
import com.food.ordering.users.infrastructure.adapter.input.rest.dto.UpdateUserRequest;
import com.food.ordering.users.infrastructure.adapter.input.rest.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          GetUserUseCase getUserUseCase,
                          UpdateUserUseCase updateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address(),
                request.role()
        );
        UserResponse response = registerUserUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponse response = getUserUseCase.getById(id);
        return ResponseEntity.ok(toDto(response));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getByEmail(@PathVariable String email) {
        UserResponse response = getUserUseCase.getByEmail(email);
        return ResponseEntity.ok(toDto(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id,
                                                   @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address()
        );
        UserResponse response = updateUserUseCase.update(id, command);
        return ResponseEntity.ok(toDto(response));
    }

    private UserResponseDto toDto(UserResponse response) {
        return new UserResponseDto(
                response.id(),
                response.email(),
                response.firstName(),
                response.lastName(),
                response.phone(),
                response.address(),
                response.role(),
                response.active()
        );
    }
}
