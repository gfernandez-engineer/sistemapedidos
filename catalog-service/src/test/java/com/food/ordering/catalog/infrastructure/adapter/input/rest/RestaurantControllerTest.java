package com.food.ordering.catalog.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase;
import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase.RestaurantResponse;
import com.food.ordering.catalog.infrastructure.adapter.input.rest.dto.CreateRestaurantRequest;
import com.food.ordering.catalog.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantController.class)
@Import(SecurityConfig.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManageRestaurantUseCase manageRestaurantUseCase;

    @Test
    void shouldReturnRestaurantById() throws Exception {
        RestaurantResponse response = new RestaurantResponse(
                1L, "Pizza Palace", "Best pizza", "123 Main St",
                "+1234567890", "Italian", BigDecimal.valueOf(4.5), true
        );
        when(manageRestaurantUseCase.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pizza Palace"))
                .andExpect(jsonPath("$.cuisineType").value("Italian"));
    }

    @Test
    void shouldReturnAllRestaurantsPaginated() throws Exception {
        RestaurantResponse response = new RestaurantResponse(
                1L, "Pizza Palace", "Best pizza", "123 Main St",
                "+1234567890", "Italian", BigDecimal.valueOf(4.5), true
        );
        Page<RestaurantResponse> page = new PageImpl<>(List.of(response));
        when(manageRestaurantUseCase.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Pizza Palace"));
    }

    @Test
    @WithMockUser
    void shouldCreateRestaurant() throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Pizza Palace", "Best pizza", "123 Main St",
                "+1234567890", "Italian"
        );
        RestaurantResponse response = new RestaurantResponse(
                1L, "Pizza Palace", "Best pizza", "123 Main St",
                "+1234567890", "Italian", BigDecimal.ZERO, true
        );
        when(manageRestaurantUseCase.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pizza Palace"));
    }
}
