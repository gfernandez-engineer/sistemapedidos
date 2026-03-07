package com.food.ordering.catalog.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase;
import com.food.ordering.catalog.application.port.input.ManageProductUseCase.ProductResponse;
import com.food.ordering.catalog.infrastructure.adapter.input.rest.dto.CreateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ProductController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageProductUseCase manageProductUseCase;

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, 1L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg", true
        );
        when(manageProductUseCase.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Margherita Pizza"))
                .andExpect(jsonPath("$.price").value(12.99));
    }

    @Test
    void shouldReturnProductsByRestaurantId() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, 1L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg", true
        );
        when(manageProductUseCase.getByRestaurantId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/restaurants/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].restaurantId").value(1));
    }

    @Test

    void shouldCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg"
        );
        ProductResponse response = new ProductResponse(
                1L, 1L, "Margherita Pizza", "Classic pizza",
                BigDecimal.valueOf(12.99), "Pizza", "http://img.com/pizza.jpg", true
        );
        when(manageProductUseCase.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/restaurants/1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Margherita Pizza"));
    }
}
