package com.food.ordering.deliveries.application.service;

import com.food.ordering.deliveries.application.port.input.RegisterDriverCommand;
import com.food.ordering.deliveries.application.port.output.DriverRepositoryPort;
import com.food.ordering.deliveries.domain.model.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepositoryPort driverRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    void register_shouldCreateDriverAsAvailable() {
        RegisterDriverCommand command = new RegisterDriverCommand("Ana Garcia", "+34698765432", "CAR");

        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        Driver result = driverService.register(command);

        assertNotNull(result);
        assertEquals("Ana Garcia", result.getName());
        assertEquals("+34698765432", result.getPhone());
        assertEquals("CAR", result.getVehicleType());
        assertTrue(result.isAvailable());
    }

    @Test
    void getAvailableDrivers_shouldReturnOnlyAvailable() {
        Driver driver1 = new Driver(1L, "Driver A", "+341111", "MOTORCYCLE", true, null);
        Driver driver2 = new Driver(2L, "Driver B", "+342222", "CAR", true, null);

        when(driverRepository.findAvailableDrivers()).thenReturn(List.of(driver1, driver2));

        List<Driver> result = driverService.getAvailableDrivers();

        assertEquals(2, result.size());
        verify(driverRepository).findAvailableDrivers();
    }

    @Test
    void toggleAvailability_shouldSwitchFromAvailableToUnavailable() {
        Driver driver = new Driver(1L, "Carlos", "+34612345678", "MOTORCYCLE", true, null);

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Driver result = driverService.toggleAvailability(1L);

        assertFalse(result.isAvailable());

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        assertFalse(captor.getValue().isAvailable());
    }

    @Test
    void toggleAvailability_shouldSwitchFromUnavailableToAvailable() {
        Driver driver = new Driver(1L, "Carlos", "+34612345678", "MOTORCYCLE", false, null);

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Driver result = driverService.toggleAvailability(1L);

        assertTrue(result.isAvailable());
    }

    @Test
    void toggleAvailability_shouldThrowWhenDriverNotFound() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> driverService.toggleAvailability(99L));
    }

    @Test
    @DisplayName("Should register driver as available by default - happy path")
    void shouldRegisterDriverAsAvailableHappyPath() {
        // Given
        RegisterDriverCommand command = new RegisterDriverCommand("Maria Torres", "+34611223344", "BICYCLE");

        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.setId(5L);
            return d;
        });

        // When
        Driver result = driverService.register(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Torres");
        assertThat(result.getPhone()).isEqualTo("+34611223344");
        assertThat(result.getVehicleType()).isEqualTo("BICYCLE");
        assertThat(result.isAvailable()).isTrue();

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        assertThat(captor.getValue().isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should return list of available drivers")
    void shouldReturnListOfAvailableDrivers() {
        // Given
        Driver driver1 = new Driver(10L, "Driver X", "+340001", "CAR", true, null);
        Driver driver2 = new Driver(11L, "Driver Y", "+340002", "MOTORCYCLE", true, null);
        Driver driver3 = new Driver(12L, "Driver Z", "+340003", "BICYCLE", true, null);

        when(driverRepository.findAvailableDrivers()).thenReturn(List.of(driver1, driver2, driver3));

        // When
        List<Driver> result = driverService.getAvailableDrivers();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Driver::getName)
                .containsExactly("Driver X", "Driver Y", "Driver Z");

        verify(driverRepository).findAvailableDrivers();
    }

    @Test
    @DisplayName("Should toggle driver availability from available to unavailable")
    void shouldToggleAvailabilityFlag() {
        // Given
        Driver driver = new Driver(1L, "Pedro", "+34600000000", "CAR", true, null);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Driver result = driverService.toggleAvailability(1L);

        // Then
        assertThat(result.isAvailable()).isFalse();

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        assertThat(captor.getValue().isAvailable()).isFalse();
    }
}
