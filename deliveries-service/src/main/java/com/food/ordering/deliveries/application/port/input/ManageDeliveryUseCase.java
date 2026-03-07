package com.food.ordering.deliveries.application.port.input;

public interface ManageDeliveryUseCase {

    DeliveryResponse create(CreateDeliveryCommand command);

    DeliveryResponse getById(Long id);

    DeliveryResponse getByOrderId(String orderId);

    DeliveryResponse updateStatus(Long id, String status);
}
