package com.food.ordering.payments.application.service;

import com.food.ordering.payments.application.port.input.dto.PaymentResponse;
import com.food.ordering.payments.application.port.input.dto.ProcessPaymentCommand;
import com.food.ordering.payments.application.port.output.PaymentEventPublisherPort;
import com.food.ordering.payments.application.port.output.PaymentRepositoryPort;
import com.food.ordering.payments.domain.exception.PaymentNotFoundException;
import com.food.ordering.payments.domain.model.Payment;
import com.food.ordering.payments.domain.model.PaymentMethod;
import com.food.ordering.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private PaymentEventPublisherPort eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        samplePayment = new Payment();
        samplePayment.setId(1L);
        samplePayment.setOrderId("order-123");
        samplePayment.setUserId("user-456");
        samplePayment.setAmount(new BigDecimal("99.99"));
        samplePayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        samplePayment.setStatus(new PaymentStatus.Completed());
        samplePayment.setTransactionId("txn-uuid-001");
        samplePayment.setCreatedAt(LocalDateTime.now());
        samplePayment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should process payment successfully - happy path")
    void shouldProcessPaymentSuccessfully() {
        // Given
        ProcessPaymentCommand command = new ProcessPaymentCommand(
                "order-123", "user-456", new BigDecimal("99.99"), PaymentMethod.CREDIT_CARD
        );

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // When
        PaymentResponse response = paymentService.process(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.userId()).isEqualTo("user-456");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.transactionId()).isNotNull();

        verify(eventPublisher).publishPaymentCompleted(any(Payment.class));
    }

    @Test
    @DisplayName("Should refund a completed payment")
    void shouldRefundCompletedPayment() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.refund(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("REFUNDED");
        assertThat(response.orderId()).isEqualTo("order-123");

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isInstanceOf(PaymentStatus.Refunded.class);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment not found")
    void shouldThrowWhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> paymentService.getById(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment not found by orderId")
    void shouldThrowWhenPaymentNotFoundByOrderId() {
        // Given
        when(paymentRepository.findByOrderId("order-nonexistent")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> paymentService.getByOrderId("order-nonexistent"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("order-nonexistent");
    }

    @Test
    @DisplayName("Should get payment by id successfully")
    void shouldGetPaymentByIdSuccessfully() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        // When
        PaymentResponse response = paymentService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.userId()).isEqualTo("user-456");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should get payment by orderId successfully")
    void shouldGetPaymentByOrderIdSuccessfully() {
        // Given
        when(paymentRepository.findByOrderId("order-123")).thenReturn(Optional.of(samplePayment));

        // When
        PaymentResponse response = paymentService.getByOrderId("order-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.userId()).isEqualTo("user-456");
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should save payment three times during processing - pending, processing, completed")
    void shouldSavePaymentThreeTimesDuringProcessing() {
        // Given
        ProcessPaymentCommand command = new ProcessPaymentCommand(
                "order-789", "user-111", new BigDecimal("50.00"), PaymentMethod.DEBIT_CARD
        );

        java.util.List<String> capturedStatuses = new java.util.ArrayList<>();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(2L);
            capturedStatuses.add(p.getStatus().name());
            return p;
        });

        // When
        PaymentResponse response = paymentService.process(command);

        // Then
        assertThat(capturedStatuses).hasSize(3);
        assertThat(capturedStatuses.get(0)).isEqualTo("PENDING");
        assertThat(capturedStatuses.get(1)).isEqualTo("PROCESSING");
        assertThat(capturedStatuses.get(2)).isEqualTo("COMPLETED");

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.transactionId()).isNotNull();
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when refunding non-existent payment")
    void shouldThrowWhenRefundingNonExistentPayment() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> paymentService.refund(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("999");
    }
}
