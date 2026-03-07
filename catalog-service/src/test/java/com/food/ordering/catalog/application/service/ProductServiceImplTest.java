package com.food.ordering.catalog.application.service;

import com.food.ordering.catalog.application.port.input.ManageProductUseCase.CreateProductCommand;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase.ProductResponse;
import com.food.ordering.catalog.application.port.output.ProductRepositoryPort;
import com.food.ordering.catalog.application.port.output.RestaurantRepositoryPort;
import com.food.ordering.catalog.domain.exception.ProductNotFoundException;
import com.food.ordering.catalog.domain.model.Product;
import com.food.ordering.catalog.domain.model.Restaurant;
import com.food.ordering.catalog.domain.exception.RestaurantNotFoundException;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(
                1L, "Pizza Palace", "Best pizza", "123 Main St",
                "+1234567890", "Italian", BigDecimal.valueOf(4.5), true,
                LocalDateTime.now(), LocalDateTime.now()
        );
        product = new Product(
                1L, 1L, "Margherita Pizza", "Classic pizza", BigDecimal.valueOf(12.99),
                "Pizza", "http://img.com/pizza.jpg", true,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() {
        CreateProductCommand command = new CreateProductCommand(
                1L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg"
        );
        when(restaurantRepositoryPort.findById(1L)).thenReturn(Optional.of(restaurant));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.create(command);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Margherita Pizza");
        assertThat(response.restaurantId()).isEqualTo(1L);
        assertThat(response.available()).isTrue();
    }

    @Test
    @DisplayName("Should get products by restaurant id")
    void shouldGetProductsByRestaurantId() {
        when(productRepositoryPort.findByRestaurantId(1L)).thenReturn(List.of(product));

        List<ProductResponse> results = productService.getByRestaurantId(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).restaurantId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should toggle product availability")
    void shouldToggleAvailability() {
        Product toggledProduct = new Product(
                1L, 1L, "Margherita Pizza", "Classic pizza", BigDecimal.valueOf(12.99),
                "Pizza", "http://img.com/pizza.jpg", false,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(toggledProduct);

        ProductResponse response = productService.toggleAvailability(1L);

        assertThat(response).isNotNull();
        assertThat(response.available()).isFalse();
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should throw RestaurantNotFoundException when creating product with non-existing restaurant")
    void shouldThrowWhenCreatingProductWithNonExistingRestaurant() {
        CreateProductCommand command = new CreateProductCommand(
                999L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg"
        );
        when(restaurantRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(command))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create product with all response fields populated correctly")
    void shouldCreateProductWithAllFields() {
        CreateProductCommand command = new CreateProductCommand(
                1L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg"
        );
        when(restaurantRepositoryPort.findById(1L)).thenReturn(Optional.of(restaurant));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.create(command);

        assertThat(response.description()).isEqualTo("Classic pizza");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(12.99));
        assertThat(response.category()).isEqualTo("Pizza");
        assertThat(response.imageUrl()).isEqualTo("http://img.com/pizza.jpg");
    }

    @Test
    @DisplayName("Should return empty list when no products for restaurant")
    void shouldReturnEmptyListWhenNoProductsForRestaurant() {
        when(productRepositoryPort.findByRestaurantId(999L)).thenReturn(List.of());

        List<ProductResponse> results = productService.getByRestaurantId(999L);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when toggling availability of non-existing product")
    void shouldThrowWhenTogglingAvailabilityOfNonExistingProduct() {
        when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.toggleAvailability(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }
}
