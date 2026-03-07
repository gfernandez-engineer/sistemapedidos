package com.food.ordering.deliveries.application.service;

import com.food.ordering.deliveries.application.port.input.CreateDeliveryCommand;
import com.food.ordering.deliveries.application.port.input.DeliveryResponse;
import com.food.ordering.deliveries.application.port.output.DeliveryEventPublisherPort;
import com.food.ordering.deliveries.application.port.output.DeliveryRepositoryPort;
import com.food.ordering.deliveries.application.port.output.DriverRepositoryPort;
import com.food.ordering.deliveries.domain.exception.DeliveryNotFoundException;
import com.food.ordering.deliveries.domain.exception.InvalidDeliveryStateException;
import com.food.ordering.deliveries.domain.exception.NoAvailableDriverException;
import com.food.ordering.deliveries.domain.model.Delivery;
import com.food.ordering.deliveries.domain.model.DeliveryStatus;
import com.food.ordering.deliveries.domain.model.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private DeliveryRepositoryPort deliveryRepository;

    @Mock
    private DriverRepositoryPort driverRepository;

    @Mock
    private DeliveryEventPublisherPort eventPublisher;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private Driver availableDriver;
    private Delivery existingDelivery;

    @BeforeEach
    void setUp() {
        availableDriver = new Driver(1L, "Carlos Lopez", "+34612345678", "MOTORCYCLE", true, "40.4168,-3.7038");

        existingDelivery = new Delivery();
        existingDelivery.setId(1L);
        existingDelivery.setOrderId("order-123");
        existingDelivery.setDriverId(1L);
        existingDelivery.setDeliveryAddress("Calle Mayor 10, Madrid");
        existingDelivery.setStatus(new DeliveryStatus.Assigned());
        existingDelivery.setEstimatedDeliveryTime(Instant.now().plusSeconds(1800));
        existingDelivery.setCreatedAt(Instant.now());
        existingDelivery.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldCreateDeliveryAndAssignDriver() {
        CreateDeliveryCommand command = new CreateDeliveryCommand("order-456", "Calle Gran Via 25, Madrid");

        when(driverRepository.findFirstAvailable()).thenReturn(Optional.of(availableDriver));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            d.setId(2L);
            return d;
        });
        when(driverRepository.save(any(Driver.class))).thenReturn(availableDriver);

        DeliveryResponse response = deliveryService.create(command);

        assertNotNull(response);
        assertEquals("order-456", response.orderId());
        assertEquals(1L, response.driverId());
        assertEquals("ASSIGNED", response.status());

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertFalse(driverCaptor.getValue().isAvailable());

        verify(eventPublisher).publishDeliveryAssigned(any(Delivery.class));
    }

    @Test
    void create_shouldThrowWhenNoDriversAvailable() {
        CreateDeliveryCommand command = new CreateDeliveryCommand("order-789", "Calle Sol 5, Madrid");

        when(driverRepository.findFirstAvailable()).thenReturn(Optional.empty());

        assertThrows(NoAvailableDriverException.class, () -> deliveryService.create(command));

        verify(deliveryRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeliveryAssigned(any());
    }

    @Test
    void getById_shouldReturnDelivery() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));

        DeliveryResponse response = deliveryService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("order-123", response.orderId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DeliveryNotFoundException.class, () -> deliveryService.getById(99L));
    }

    @Test
    void updateStatus_shouldTransitionToPickedUp() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(1L, "PICKED_UP");

        assertNotNull(response);
        assertEquals("PICKED_UP", response.status());
        verify(eventPublisher).publishDeliveryStatusChanged(any(Delivery.class));
    }

    @Test
    void updateStatus_shouldThrowOnInvalidTransition() {
        existingDelivery.setStatus(new DeliveryStatus.Delivered());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));

        assertThrows(InvalidDeliveryStateException.class,
                () -> deliveryService.updateStatus(1L, "ASSIGNED"));

        verify(deliveryRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeliveryStatusChanged(any());
    }

    @Test
    void updateStatus_shouldSetActualDeliveryTimeWhenDelivered() {
        existingDelivery.setStatus(new DeliveryStatus.InTransit());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(1L, "DELIVERED");

        assertEquals("DELIVERED", response.status());
        assertNotNull(response.actualDeliveryTime());
    }

    @Test
    @DisplayName("Should create delivery and publish event - happy path with AssertJ")
    void shouldCreateDeliveryAndPublishEvent() {
        // Given
        CreateDeliveryCommand command = new CreateDeliveryCommand("order-500", "Calle Serrano 20, Madrid");

        when(driverRepository.findFirstAvailable()).thenReturn(Optional.of(availableDriver));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });
        when(driverRepository.save(any(Driver.class))).thenReturn(availableDriver);

        // When
        DeliveryResponse response = deliveryService.create(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("order-500");
        assertThat(response.driverId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("ASSIGNED");
        assertThat(response.deliveryAddress()).isEqualTo("Calle Serrano 20, Madrid");
        assertThat(response.estimatedDeliveryTime()).isNotNull();

        verify(eventPublisher).publishDeliveryAssigned(any(Delivery.class));
    }

    @Test
    @DisplayName("Should throw NoAvailableDriverException when no drivers available")
    void shouldThrowNoAvailableDriverException() {
        // Given
        CreateDeliveryCommand command = new CreateDeliveryCommand("order-600", "Calle Luna 3, Madrid");
        when(driverRepository.findFirstAvailable()).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> deliveryService.create(command))
                .isInstanceOf(NoAvailableDriverException.class);

        verify(deliveryRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeliveryAssigned(any());
    }

    @Test
    @DisplayName("Should throw DeliveryNotFoundException when delivery not found by id")
    void shouldThrowDeliveryNotFoundExceptionById() {
        // Given
        when(deliveryRepository.findById(404L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> deliveryService.getById(404L))
                .isInstanceOf(DeliveryNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw InvalidDeliveryStateException on invalid status transition")
    void shouldThrowInvalidDeliveryStateExceptionOnInvalidTransition() {
        // Given
        existingDelivery.setStatus(new DeliveryStatus.Delivered());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));

        // When / Then
        assertThatThrownBy(() -> deliveryService.updateStatus(1L, "ASSIGNED"))
                .isInstanceOf(InvalidDeliveryStateException.class);

        verify(deliveryRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeliveryStatusChanged(any());
    }

    @Test
    @DisplayName("Should set actualDeliveryTime when status transitions to DELIVERED")
    void shouldSetActualDeliveryTimeWhenDelivered() {
        // Given
        existingDelivery.setStatus(new DeliveryStatus.InTransit());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DeliveryResponse response = deliveryService.updateStatus(1L, "DELIVERED");

        // Then
        assertThat(response.status()).isEqualTo("DELIVERED");
        assertThat(response.actualDeliveryTime()).isNotNull();

        ArgumentCaptor<Delivery> captor = ArgumentCaptor.forClass(Delivery.class);
        verify(deliveryRepository).save(captor.capture());
        assertThat(captor.getValue().getActualDeliveryTime()).isNotNull();
        assertThat(captor.getValue().getStatus()).isInstanceOf(DeliveryStatus.Delivered.class);
    }
}
