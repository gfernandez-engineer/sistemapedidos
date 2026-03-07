package com.food.ordering.users.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.users.application.port.input.GetUserUseCase;
import com.food.ordering.users.application.port.input.RegisterUserUseCase;
import com.food.ordering.users.application.port.input.UpdateUserUseCase;
import com.food.ordering.users.application.port.input.response.UserResponse;
import com.food.ordering.users.domain.exception.UserNotFoundException;
import com.food.ordering.users.infrastructure.adapter.input.rest.dto.RegisterUserRequest;
import com.food.ordering.users.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @Test
    @DisplayName("POST /api/v1/users should return 201 Created")
    void register_ValidRequest_Returns201() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                "123 Main St",
                "CUSTOMER"
        );

        UserResponse response = new UserResponse(
                1L, "john@example.com", "John", "Doe",
                "+1234567890", "123 Main St", "CUSTOMER", true
        );

        when(registerUserUseCase.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return 200 OK")
    void getById_ExistingUser_Returns200() throws Exception {
        UserResponse response = new UserResponse(
                1L, "john@example.com", "John", "Doe",
                "+1234567890", "123 Main St", "CUSTOMER", true
        );

        when(getUserUseCase.getById(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return 404 when not found")
    void getById_NonExistingUser_Returns404() throws Exception {
        when(getUserUseCase.getById(eq(99L)))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("User Not Found"));
    }
}
