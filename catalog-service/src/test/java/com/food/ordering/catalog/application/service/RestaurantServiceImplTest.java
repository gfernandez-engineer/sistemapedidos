package com.food.ordering.catalog.application.service;

import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase.CreateRestaurantCommand;
import com.food.ordering.catalog.application.port.input.ManageRestaurantUseCase.RestaurantResponse;
import com.food.ordering.catalog.application.port.output.RestaurantRepositoryPort;
import com.food.ordering.catalog.domain.exception.RestaurantNotFoundException;
import com.food.ordering.catalog.domain.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(
                1L, "Pizza Palace", "Best pizza in town", "123 Main St",
                "+1234567890", "Italian", BigDecimal.valueOf(4.5), true,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create restaurant successfully")
    void shouldCreateRestaurant() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Pizza Palace", "Best pizza in town", "123 Main St",
                "+1234567890", "Italian"
        );
        when(restaurantRepositoryPort.save(any(Restaurant.class))).thenReturn(restaurant);

        RestaurantResponse response = restaurantService.create(command);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Pizza Palace");
        assertThat(response.cuisineType()).isEqualTo("Italian");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("Should get restaurant by id")
    void shouldGetRestaurantById() {
        when(restaurantRepositoryPort.findById(1L)).thenReturn(Optional.of(restaurant));

        RestaurantResponse response = restaurantService.getById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Pizza Palace");
    }

    @Test
    @DisplayName("Should throw RestaurantNotFoundException when restaurant not found")
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getById(99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should search restaurants by cuisine type")
    void shouldSearchByCuisineType() {
        when(restaurantRepositoryPort.findByCuisineType("Italian"))
                .thenReturn(List.of(restaurant));

        List<RestaurantResponse> results = restaurantService.searchByCuisineType("Italian");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).cuisineType()).isEqualTo("Italian");
    }

    @Test
    @DisplayName("Should return empty list when no restaurants match cuisine type")
    void shouldReturnEmptyListWhenNoCuisineMatch() {
        when(restaurantRepositoryPort.findByCuisineType("Japanese"))
                .thenReturn(List.of());

        List<RestaurantResponse> results = restaurantService.searchByCuisineType("Japanese");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should create restaurant with correct response fields")
    void shouldCreateRestaurantWithCorrectFields() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Pizza Palace", "Best pizza in town", "123 Main St",
                "+1234567890", "Italian"
        );
        when(restaurantRepositoryPort.save(any(Restaurant.class))).thenReturn(restaurant);

        RestaurantResponse response = restaurantService.create(command);

        assertThat(response.description()).isEqualTo("Best pizza in town");
        assertThat(response.address()).isEqualTo("123 Main St");
        assertThat(response.phone()).isEqualTo("+1234567890");
        assertThat(response.rating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
    }
}
