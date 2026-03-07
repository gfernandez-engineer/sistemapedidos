package com.food.ordering.orders.infrastructure.adapter.input.rest;

import com.food.ordering.orders.application.port.input.CreateOrderUseCase;
import com.food.ordering.orders.application.port.input.GetOrderUseCase;
import com.food.ordering.orders.application.port.input.UpdateOrderStatusUseCase;
import com.food.ordering.orders.application.port.input.command.CreateOrderCommand;
import com.food.ordering.orders.application.port.input.command.OrderItemCommand;
import com.food.ordering.orders.application.port.input.response.OrderResponse;
import com.food.ordering.orders.infrastructure.adapter.input.rest.dto.CreateOrderRequest;
import com.food.ordering.orders.infrastructure.adapter.input.rest.dto.OrderResponseDto;
import com.food.ordering.orders.infrastructure.adapter.input.rest.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.userId(),
                request.restaurantId(),
                request.items().stream()
                        .map(item -> new OrderItemCommand(
                                item.productId(),
                                item.productName(),
                                item.quantity(),
                                item.unitPrice()
                        ))
                        .toList(),
                request.deliveryAddress(),
                request.notes()
        );

        OrderResponse response = createOrderUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        OrderResponse response = getOrderUseCase.getById(id);
        return ResponseEntity.ok(toDto(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderResponseDto> responses = getOrderUseCase.getByUserId(userId).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = updateOrderStatusUseCase.updateStatus(id, request.status());
        return ResponseEntity.ok(toDto(response));
    }

    private OrderResponseDto toDto(OrderResponse response) {
        List<OrderResponseDto.OrderItemResponseDto> itemDtos = response.items().stream()
                .map(item -> new OrderResponseDto.OrderItemResponseDto(
                        item.id(),
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.unitPrice()
                ))
                .toList();

        return new OrderResponseDto(
                response.id(),
                response.userId(),
                response.restaurantId(),
                itemDtos,
                response.status(),
                response.totalAmount(),
                response.deliveryAddress(),
                response.notes(),
                response.createdAt()
        );
    }
}
