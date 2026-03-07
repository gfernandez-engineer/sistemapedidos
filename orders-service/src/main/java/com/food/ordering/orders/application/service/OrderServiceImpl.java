package com.food.ordering.orders.application.service;

import com.food.ordering.orders.application.port.input.CreateOrderUseCase;
import com.food.ordering.orders.application.port.input.GetOrderUseCase;
import com.food.ordering.orders.application.port.input.UpdateOrderStatusUseCase;
import com.food.ordering.orders.application.port.input.command.CreateOrderCommand;
import com.food.ordering.orders.application.port.input.response.OrderItemResponse;
import com.food.ordering.orders.application.port.input.response.OrderResponse;
import com.food.ordering.orders.application.port.output.OrderEventPublisherPort;
import com.food.ordering.orders.application.port.output.OrderRepositoryPort;
import com.food.ordering.orders.domain.exception.InvalidOrderStateException;
import com.food.ordering.orders.domain.exception.OrderNotFoundException;
import com.food.ordering.orders.domain.model.Order;
import com.food.ordering.orders.domain.model.OrderItem;
import com.food.ordering.orders.domain.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements CreateOrderUseCase, GetOrderUseCase, UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventPublisherPort eventPublisher;

    public OrderServiceImpl(OrderRepositoryPort orderRepository, OrderEventPublisherPort eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse create(CreateOrderCommand command) {
        Order order = new Order();
        order.setUserId(command.userId());
        order.setRestaurantId(command.restaurantId());
        order.setDeliveryAddress(command.deliveryAddress());
        order.setNotes(command.notes());
        order.setStatus(new OrderStatus.Pending());

        List<OrderItem> items = command.items().stream()
                .map(itemCmd -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemCmd.productId());
                    item.setProductName(itemCmd.productName());
                    item.setQuantity(itemCmd.quantity());
                    item.setUnitPrice(itemCmd.unitPrice());
                    return item;
                })
                .toList();

        order.setItems(items);
        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishOrderCreated(savedOrder);

        return toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateStatus(Long id, String newStatusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.fromString(newStatusStr);

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(currentStatus.name(), newStatus.name());
        }

        String previousStatus = currentStatus.name();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        eventPublisher.publishOrderStatusChanged(updatedOrder, previousStatus);

        return toResponse(updatedOrder);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                itemResponses,
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getNotes(),
                order.getCreatedAt()
        );
    }
}
