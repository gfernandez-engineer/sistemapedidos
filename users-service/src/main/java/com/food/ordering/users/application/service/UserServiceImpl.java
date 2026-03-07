package com.food.ordering.users.application.service;

import com.food.ordering.users.application.port.input.GetUserUseCase;
import com.food.ordering.users.application.port.input.RegisterUserUseCase;
import com.food.ordering.users.application.port.input.UpdateUserUseCase;
import com.food.ordering.users.application.port.input.command.RegisterUserCommand;
import com.food.ordering.users.application.port.input.command.UpdateUserCommand;
import com.food.ordering.users.application.port.input.response.UserResponse;
import com.food.ordering.users.application.port.output.UserRepositoryPort;
import com.food.ordering.users.domain.exception.UserAlreadyExistsException;
import com.food.ordering.users.domain.exception.UserNotFoundException;
import com.food.ordering.users.domain.model.User;
import com.food.ordering.users.domain.model.User.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements RegisterUserUseCase, GetUserUseCase, UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserServiceImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserResponse register(RegisterUserCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("email", command.email());
        }

        User user = new User();
        user.setEmail(command.email());
        user.setPassword(command.password());
        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setPhone(command.phone());
        user.setAddress(command.address());
        user.setRole(mapRole(command.role()));
        user.setActive(true);

        User savedUser = userRepositoryPort.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        return toResponse(user);
    }

    @Override
    public UserResponse update(Long id, UpdateUserCommand command) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setPhone(command.phone());
        user.setAddress(command.address());

        User updatedUser = userRepositoryPort.save(user);
        return toResponse(updatedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.isActive()
        );
    }

    private UserRole mapRole(String role) {
        return switch (role.toUpperCase()) {
            case "CUSTOMER" -> new UserRole.Customer();
            case "RESTAURANT_OWNER" -> new UserRole.RestaurantOwner();
            case "DELIVERY_DRIVER" -> new UserRole.DeliveryDriver();
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
