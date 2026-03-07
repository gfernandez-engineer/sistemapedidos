package com.food.ordering.orders.domain.model;

import java.util.Set;

public sealed interface OrderStatus permits
        OrderStatus.Pending,
        OrderStatus.Confirmed,
        OrderStatus.Preparing,
        OrderStatus.Ready,
        OrderStatus.InDelivery,
        OrderStatus.Delivered,
        OrderStatus.Cancelled {

    record Pending() implements OrderStatus {}
    record Confirmed() implements OrderStatus {}
    record Preparing() implements OrderStatus {}
    record Ready() implements OrderStatus {}
    record InDelivery() implements OrderStatus {}
    record Delivered() implements OrderStatus {}
    record Cancelled() implements OrderStatus {}

    default boolean canTransitionTo(OrderStatus target) {
        return getAllowedTransitions().contains(target.name());
    }

    default Set<String> getAllowedTransitions() {
        return switch (this) {
            case Pending p -> Set.of("CONFIRMED", "CANCELLED");
            case Confirmed c -> Set.of("PREPARING", "CANCELLED");
            case Preparing p -> Set.of("READY", "CANCELLED");
            case Ready r -> Set.of("IN_DELIVERY", "CANCELLED");
            case InDelivery i -> Set.of("DELIVERED");
            case Delivered d -> Set.of();
            case Cancelled c -> Set.of();
        };
    }

    default String name() {
        return switch (this) {
            case Pending p -> "PENDING";
            case Confirmed c -> "CONFIRMED";
            case Preparing p -> "PREPARING";
            case Ready r -> "READY";
            case InDelivery i -> "IN_DELIVERY";
            case Delivered d -> "DELIVERED";
            case Cancelled c -> "CANCELLED";
        };
    }

    static OrderStatus fromString(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> new Pending();
            case "CONFIRMED" -> new Confirmed();
            case "PREPARING" -> new Preparing();
            case "READY" -> new Ready();
            case "IN_DELIVERY" -> new InDelivery();
            case "DELIVERED" -> new Delivered();
            case "CANCELLED" -> new Cancelled();
            default -> throw new IllegalArgumentException("Unknown order status: " + status);
        };
    }
}
