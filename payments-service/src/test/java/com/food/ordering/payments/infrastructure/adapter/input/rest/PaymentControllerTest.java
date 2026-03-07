package com.food.ordering.payments.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.payments.application.port.input.GetPaymentUseCase;
import com.food.ordering.payments.application.port.input.ProcessPaymentUseCase;
import com.food.ordering.payments.application.port.input.RefundPaymentUseCase;
import com.food.ordering.payments.application.port.input.dto.PaymentResponse;
import com.food.ordering.payments.infrastructure.adapter.input.rest.dto.CreatePaymentRequest;
import com.food.ordering.payments.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessPaymentUseCase processPaymentUseCase;

    @MockitoBean
    private GetPaymentUseCase getPaymentUseCase;

    @MockitoBean
    private RefundPaymentUseCase refundPaymentUseCase;

    private PaymentResponse sampleResponse() {
        return new PaymentResponse(
                1L, "order-123", "user-456",
                new BigDecimal("99.99"), "CREDIT_CARD", "COMPLETED",
                "txn-uuid-001", LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/v1/payments should return 201 Created")
    @WithMockUser
    void shouldCreatePaymentAndReturn201() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "order-123", "user-456", new BigDecimal("99.99"), "CREDIT_CARD"
        );

        when(processPaymentUseCase.process(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} should return 200 OK")
    @WithMockUser
    void shouldGetPaymentByIdAndReturn200() throws Exception {
        when(getPaymentUseCase.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("order-123"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/order/{orderId} should return 200 OK")
    @WithMockUser
    void shouldGetPaymentByOrderIdAndReturn200() throws Exception {
        when(getPaymentUseCase.getByOrderId("order-123")).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/payments/order/order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/{id}/refund should return 200 OK")
    @WithMockUser
    void shouldRefundPaymentAndReturn200() throws Exception {
        PaymentResponse refundResponse = new PaymentResponse(
                1L, "order-123", "user-456",
                new BigDecimal("99.99"), "CREDIT_CARD", "REFUNDED",
                "txn-uuid-001", LocalDateTime.now()
        );

        when(refundPaymentUseCase.refund(1L)).thenReturn(refundResponse);

        mockMvc.perform(post("/api/v1/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
