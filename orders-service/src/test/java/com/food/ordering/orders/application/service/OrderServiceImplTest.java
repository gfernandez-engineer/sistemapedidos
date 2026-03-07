package com.food.ordering.orders.application.service;

import com.food.ordering.orders.application.port.input.command.CreateOrderCommand;
import com.food.ordering.orders.application.port.input.command.OrderItemCommand;
import com.food.ordering.orders.application.port.input.response.OrderResponse;
import com.food.ordering.orders.application.port.output.OrderEventPublisherPort;
import com.food.ordering.orders.application.port.output.OrderRepositoryPort;
import com.food.ordering.orders.domain.exception.InvalidOrderStateException;
import com.food.ordering.orders.domain.exception.OrderNotFoundException;
import com.food.ordering.orders.domain.model.Order;
import com.food.ordering.orders.domain.model.OrderItem;
import com.food.ordering.orders.domain.model.OrderStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private OrderEventPublisherPort eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setUserId(100L);
        sampleOrder.setRestaurantId(200L);
        sampleOrder.setDeliveryAddress("123 Main St");
        sampleOrder.setNotes("No onions");
        sampleOrder.setStatus(new OrderStatus.Pending());
        sampleOrder.setCreatedAt(LocalDateTime.now());
        sampleOrder.setUpdatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(10L);
        item.setProductName("Burger");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("9.99"));
        sampleOrder.setItems(List.of(item));
        sampleOrder.setTotalAmount(new BigDecimal("19.98"));
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrder() {
        CreateOrderCommand command = new CreateOrderCommand(
                100L, 200L,
                List.of(new OrderItemCommand(10L, "Burger", 2, new BigDecimal("9.99"))),
                "123 Main St", "No onions"
        );

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.create(command);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(100L, response.userId());
        assertEquals(200L, response.restaurantId());
        assertEquals("PENDING", response.status());
        assertEquals(1, response.items().size());
        assertEquals("Burger", response.items().getFirst().productName());

        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(sampleOrder);
    }

    @Test
    @DisplayName("Should update order status with valid transition")
    void shouldUpdateStatusValidTransition() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        Order confirmedOrder = new Order();
        confirmedOrder.setId(1L);
        confirmedOrder.setUserId(100L);
        confirmedOrder.setRestaurantId(200L);
        confirmedOrder.setStatus(new OrderStatus.Confirmed());
        confirmedOrder.setItems(sampleOrder.getItems());
        confirmedOrder.setTotalAmount(sampleOrder.getTotalAmount());
        confirmedOrder.setDeliveryAddress(sampleOrder.getDeliveryAddress());
        confirmedOrder.setNotes(sampleOrder.getNotes());
        confirmedOrder.setCreatedAt(sampleOrder.getCreatedAt());
        confirmedOrder.setUpdatedAt(LocalDateTime.now());

        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        OrderResponse response = orderService.updateStatus(1L, "CONFIRMED");

        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(eventPublisher).publishOrderStatusChanged(any(Order.class), eq("PENDING"));
    }

    @Test
    @DisplayName("Should throw exception for invalid state transition")
    void shouldThrowForInvalidTransition() {
        sampleOrder.setStatus(new OrderStatus.Delivered());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateStatus(1L, "PENDING"));

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishOrderStatusChanged(any(), anyString());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getById(999L));
    }

    @Test
    @DisplayName("Should get order by id")
    void shouldGetOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("PENDING", response.status());
    }

    @Test
    @DisplayName("Should get orders by user id")
    void shouldGetOrdersByUserId() {
        when(orderRepository.findByUserId(100L)).thenReturn(List.of(sampleOrder));

        List<OrderResponse> responses = orderService.getByUserId(100L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.getFirst().userId());
    }

    @Test
    @DisplayName("Should create order with correct total amount and item details")
    void shouldCreateOrderWithCorrectDetails() {
        CreateOrderCommand command = new CreateOrderCommand(
                100L, 200L,
                List.of(new OrderItemCommand(10L, "Burger", 2, new BigDecimal("9.99"))),
                "123 Main St", "No onions"
        );

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.create(command);

        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("19.98"));
        assertThat(response.deliveryAddress()).isEqualTo("123 Main St");
        assertThat(response.notes()).isEqualTo("No onions");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.items().getFirst().unitPrice()).isEqualByComparingTo(new BigDecimal("9.99"));
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException with message containing order id")
    void shouldThrowOrderNotFoundWithMessage() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void shouldReturnEmptyListWhenUserHasNoOrders() {
        when(orderRepository.findByUserId(999L)).thenReturn(List.of());

        List<OrderResponse> responses = orderService.getByUserId(999L);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should throw InvalidOrderStateException when transitioning from Cancelled")
    void shouldThrowForTransitionFromCancelled() {
        sampleOrder.setStatus(new OrderStatus.Cancelled());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "CONFIRMED"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("CANCELLED")
                .hasMessageContaining("CONFIRMED");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishOrderStatusChanged(any(), anyString());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when updating status of non-existing order")
    void shouldThrowWhenUpdatingStatusOfNonExistingOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(999L, "CONFIRMED"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return multiple orders for user")
    void shouldReturnMultipleOrdersForUser() {
        Order secondOrder = new Order();
        secondOrder.setId(2L);
        secondOrder.setUserId(100L);
        secondOrder.setRestaurantId(300L);
        secondOrder.setStatus(new OrderStatus.Confirmed());
        secondOrder.setItems(List.of());
        secondOrder.setTotalAmount(new BigDecimal("25.00"));
        secondOrder.setDeliveryAddress("456 Oak Ave");
        secondOrder.setNotes("");
        secondOrder.setCreatedAt(LocalDateTime.now());
        secondOrder.setUpdatedAt(LocalDateTime.now());

        when(orderRepository.findByUserId(100L)).thenReturn(List.of(sampleOrder, secondOrder));

        List<OrderResponse> responses = orderService.getByUserId(100L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(OrderResponse::userId).containsOnly(100L);
        assertThat(responses).extracting(OrderResponse::id).containsExactly(1L, 2L);
    }
}
