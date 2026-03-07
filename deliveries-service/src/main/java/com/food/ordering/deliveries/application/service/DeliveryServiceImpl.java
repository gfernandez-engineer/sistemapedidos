package com.food.ordering.deliveries.application.service;

import com.food.ordering.deliveries.application.port.input.CreateDeliveryCommand;
import com.food.ordering.deliveries.application.port.input.DeliveryResponse;
import com.food.ordering.deliveries.application.port.input.ManageDeliveryUseCase;
import com.food.ordering.deliveries.application.port.output.DeliveryEventPublisherPort;
import com.food.ordering.deliveries.application.port.output.DeliveryRepositoryPort;
import com.food.ordering.deliveries.application.port.output.DriverRepositoryPort;
import com.food.ordering.deliveries.domain.exception.DeliveryNotFoundException;
import com.food.ordering.deliveries.domain.exception.InvalidDeliveryStateException;
import com.food.ordering.deliveries.domain.exception.NoAvailableDriverException;
import com.food.ordering.deliveries.domain.model.Delivery;
import com.food.ordering.deliveries.domain.model.DeliveryStatus;
import com.food.ordering.deliveries.domain.model.Driver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class DeliveryServiceImpl implements ManageDeliveryUseCase {

    private final DeliveryRepositoryPort deliveryRepository;
    private final DriverRepositoryPort driverRepository;
    private final DeliveryEventPublisherPort eventPublisher;

    public DeliveryServiceImpl(DeliveryRepositoryPort deliveryRepository,
                               DriverRepositoryPort driverRepository,
                               DeliveryEventPublisherPort eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DeliveryResponse create(CreateDeliveryCommand command) {
        Driver availableDriver = driverRepository.findFirstAvailable()
                .orElseThrow(NoAvailableDriverException::new);

        Delivery delivery = new Delivery();
        delivery.setOrderId(command.orderId());
        delivery.setDeliveryAddress(command.deliveryAddress());
        delivery.setDriverId(availableDriver.getId());
        delivery.setStatus(new DeliveryStatus.Assigned());
        delivery.setEstimatedDeliveryTime(Instant.now().plus(30, ChronoUnit.MINUTES));
        delivery.setCreatedAt(Instant.now());
        delivery.setUpdatedAt(Instant.now());

        availableDriver.setAvailable(false);
        driverRepository.save(availableDriver);

        Delivery saved = deliveryRepository.save(delivery);

        eventPublisher.publishDeliveryAssigned(saved);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));
        return toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(String orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException(orderId));
        return toResponse(delivery);
    }

    @Override
    public DeliveryResponse updateStatus(Long id, String statusStr) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));

        DeliveryStatus currentStatus = delivery.getStatus();
        DeliveryStatus targetStatus = DeliveryStatus.fromString(statusStr);

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidDeliveryStateException(currentStatus.name(), targetStatus.name());
        }

        delivery.setStatus(targetStatus);
        delivery.setUpdatedAt(Instant.now());

        if (targetStatus instanceof DeliveryStatus.Delivered) {
            delivery.setActualDeliveryTime(Instant.now());
        }

        Delivery updated = deliveryRepository.save(delivery);

        eventPublisher.publishDeliveryStatusChanged(updated);

        return toResponse(updated);
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getDriverId(),
                delivery.getDeliveryAddress(),
                delivery.getStatus().name(),
                delivery.getEstimatedDeliveryTime(),
                delivery.getActualDeliveryTime(),
                delivery.getCreatedAt()
        );
    }
}
