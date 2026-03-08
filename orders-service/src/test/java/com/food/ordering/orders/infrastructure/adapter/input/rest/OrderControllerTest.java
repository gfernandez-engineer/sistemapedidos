package com.food.ordering.orders.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.orders.application.port.input.CreateOrderUseCase;
import com.food.ordering.orders.application.port.input.GetOrderUseCase;
import com.food.ordering.orders.application.port.input.UpdateOrderStatusUseCase;
import com.food.ordering.orders.application.port.input.response.OrderItemResponse;
import com.food.ordering.orders.application.port.input.response.OrderResponse;
import com.food.ordering.orders.domain.exception.InvalidOrderStateException;
import com.food.ordering.orders.domain.exception.OrderNotFoundException;
import com.food.ordering.orders.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private GetOrderUseCase getOrderUseCase;

    @MockitoBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    private OrderResponse buildSampleResponse() {
        return new OrderResponse(
                1L, 100L, 200L,
                List.of(new OrderItemResponse(1L, 10L, "Burger", 2, new BigDecimal("9.99"))),
                "PENDING",
                new BigDecimal("19.98"),
                "123 Main St",
                "No onions",
                LocalDateTime.of(2026, 3, 6, 12, 0, 0)
        );
    }

    @Test

    @DisplayName("POST /api/v1/orders returns 201 Created")
    void createOrderReturns201() throws Exception {
        when(createOrderUseCase.create(any())).thenReturn(buildSampleResponse());

        String requestBody = """
                {
                    "userId": 100,
                    "restaurantId": 200,
                    "items": [
                        {
                            "productId": 10,
                            "productName": "Burger",
                            "quantity": 2,
                            "unitPrice": 9.99
                        }
                    ],
                    "deliveryAddress": "123 Main St",
                    "notes": "No onions"
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(19.98))
                .andExpect(jsonPath("$.items[0].productName").value("Burger"));
    }

    @Test

    @DisplayName("GET /api/v1/orders/{id} returns 200 OK")
    void getOrderReturns200() throws Exception {
        when(getOrderUseCase.getById(1L)).thenReturn(buildSampleResponse());

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.deliveryAddress").value("123 Main St"));
    }

    @Test

    @DisplayName("GET /api/v1/orders/{id} returns 404 when not found")
    void getOrderReturns404() throws Exception {
        when(getOrderUseCase.getById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order Not Found"));
    }

    @Test

    @DisplayName("PATCH /api/v1/orders/{id}/status returns 200 OK")
    void updateOrderStatusReturns200() throws Exception {
        OrderResponse confirmedResponse = new OrderResponse(
                1L, 100L, 200L,
                List.of(new OrderItemResponse(1L, 10L, "Burger", 2, new BigDecimal("9.99"))),
                "CONFIRMED",
                new BigDecimal("19.98"),
                "123 Main St",
                "No onions",
                LocalDateTime.of(2026, 3, 6, 12, 0, 0)
        );

        when(updateOrderStatusUseCase.updateStatus(eq(1L), eq("CONFIRMED"))).thenReturn(confirmedResponse);

        String requestBody = """
                {
                    "status": "CONFIRMED"
                }
                """;

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test

    @DisplayName("GET /api/v1/orders/user/{userId} returns 200 OK")
    void getOrdersByUserReturns200() throws Exception {
        when(getOrderUseCase.getByUserId(100L)).thenReturn(List.of(buildSampleResponse()));

        mockMvc.perform(get("/api/v1/orders/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(100));
    }

    @Test

    @DisplayName("PATCH /api/v1/orders/{id}/status returns 409 for invalid transition")
    void updateOrderStatusReturns409ForInvalidTransition() throws Exception {
        when(updateOrderStatusUseCase.updateStatus(eq(1L), eq("PENDING")))
                .thenThrow(new InvalidOrderStateException("DELIVERED", "PENDING"));

        String requestBody = """
                {
                    "status": "PENDING"
                }
                """;

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid Order State Transition"));
    }

    @Test

    @DisplayName("GET /api/v1/orders/user/{userId} returns empty list when no orders")
    void getOrdersByUserReturnsEmptyList() throws Exception {
        when(getOrderUseCase.getByUserId(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test

    @DisplayName("POST /api/v1/orders returns correct response fields")
    void createOrderReturnsCorrectFields() throws Exception {
        when(createOrderUseCase.create(any())).thenReturn(buildSampleResponse());

        String requestBody = """
                {
                    "userId": 100,
                    "restaurantId": 200,
                    "items": [
                        {
                            "productId": 10,
                            "productName": "Burger",
                            "quantity": 2,
                            "unitPrice": 9.99
                        }
                    ],
                    "deliveryAddress": "123 Main St",
                    "notes": "No onions"
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").value(200))
                .andExpect(jsonPath("$.deliveryAddress").value("123 Main St"))
                .andExpect(jsonPath("$.notes").value("No onions"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(9.99));
    }
}
