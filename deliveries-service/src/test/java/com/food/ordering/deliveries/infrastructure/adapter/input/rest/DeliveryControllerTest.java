package com.food.ordering.deliveries.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.deliveries.application.port.input.CreateDeliveryCommand;
import com.food.ordering.deliveries.application.port.input.DeliveryResponse;
import com.food.ordering.deliveries.application.port.input.ManageDeliveryUseCase;
import com.food.ordering.deliveries.domain.exception.DeliveryNotFoundException;
import com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto.CreateDeliveryRequest;
import com.food.ordering.deliveries.infrastructure.adapter.input.rest.dto.UpdateStatusRequest;
import com.food.ordering.deliveries.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DeliveryController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, JacksonAutoConfiguration.class})
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageDeliveryUseCase manageDeliveryUseCase;

    private final Instant now = Instant.now();

    @Test
    void createDelivery_shouldReturn201() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest("order-100", "Calle Alcala 50, Madrid");
        DeliveryResponse response = new DeliveryResponse(1L, "order-100", 5L,
                "Calle Alcala 50, Madrid", "ASSIGNED", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.create(any(CreateDeliveryCommand.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("order-100"))
                .andExpect(jsonPath("$.driverId").value(5))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void createDelivery_shouldReturn400WhenInvalid() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest("", "");

        mockMvc.perform(post("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDeliveryById_shouldReturn200() throws Exception {
        DeliveryResponse response = new DeliveryResponse(1L, "order-100", 5L,
                "Calle Alcala 50, Madrid", "IN_TRANSIT", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/deliveries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    void getDeliveryById_shouldReturn404WhenNotFound() throws Exception {
        when(manageDeliveryUseCase.getById(99L)).thenThrow(new DeliveryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/deliveries/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Delivery not found with id: 99"));
    }

    @Test
    void getDeliveryByOrderId_shouldReturn200() throws Exception {
        DeliveryResponse response = new DeliveryResponse(1L, "order-200", 3L,
                "Calle Sol 15, Madrid", "PICKED_UP", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.getByOrderId("order-200")).thenReturn(response);

        mockMvc.perform(get("/api/v1/deliveries/order/order-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-200"))
                .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("PICKED_UP");
        DeliveryResponse response = new DeliveryResponse(1L, "order-100", 5L,
                "Calle Alcala 50, Madrid", "PICKED_UP", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.updateStatus(eq(1L), eq("PICKED_UP"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/deliveries/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }

    @Test
    @DisplayName("POST /api/v1/deliveries should return 201 Created")
    void shouldCreateDeliveryAndReturn201() throws Exception {
        CreateDeliveryRequest request = new CreateDeliveryRequest("order-300", "Paseo de la Castellana 100, Madrid");
        DeliveryResponse response = new DeliveryResponse(3L, "order-300", 7L,
                "Paseo de la Castellana 100, Madrid", "ASSIGNED", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.create(any(CreateDeliveryCommand.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.orderId").value("order-300"))
                .andExpect(jsonPath("$.driverId").value(7))
                .andExpect(jsonPath("$.deliveryAddress").value("Paseo de la Castellana 100, Madrid"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("GET /api/v1/deliveries/{id} should return 200 OK")
    void shouldGetDeliveryByIdAndReturn200() throws Exception {
        DeliveryResponse response = new DeliveryResponse(2L, "order-200", 4L,
                "Calle Mayor 5, Madrid", "PICKED_UP", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.getById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/deliveries/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.orderId").value("order-200"))
                .andExpect(jsonPath("$.driverId").value(4))
                .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }

    @Test
    @DisplayName("PATCH /api/v1/deliveries/{id}/status should return 200 OK")
    void shouldUpdateStatusAndReturn200() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("IN_TRANSIT");
        DeliveryResponse response = new DeliveryResponse(1L, "order-100", 5L,
                "Calle Alcala 50, Madrid", "IN_TRANSIT", now.plusSeconds(1800), null, now);

        when(manageDeliveryUseCase.updateStatus(eq(1L), eq("IN_TRANSIT"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/deliveries/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }
}
