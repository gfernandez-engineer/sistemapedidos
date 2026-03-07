package com.food.ordering.users.application.service;

import com.food.ordering.users.application.port.input.command.RegisterUserCommand;
import com.food.ordering.users.application.port.input.response.UserResponse;
import com.food.ordering.users.application.port.output.UserRepositoryPort;
import com.food.ordering.users.domain.exception.UserAlreadyExistsException;
import com.food.ordering.users.domain.exception.UserNotFoundException;
import com.food.ordering.users.domain.model.User;
import com.food.ordering.users.domain.model.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterUserCommand registerCommand;

    @BeforeEach
    void setUp() {
        testUser = new User(
                1L,
                "john@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                "123 Main St",
                new UserRole.Customer(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        registerCommand = new RegisterUserCommand(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                "123 Main St",
                "CUSTOMER"
        );
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_HappyPath_ReturnsUserResponse() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.register(registerCommand);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when registering with duplicate email")
    void register_DuplicateEmail_ThrowsUserAlreadyExistsException() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by id")
    void getById_NotFound_ThrowsUserNotFoundException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }
}
