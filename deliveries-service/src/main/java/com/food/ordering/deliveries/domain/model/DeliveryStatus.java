package com.food.ordering.deliveries.domain.model;

import java.util.Set;

public sealed interface DeliveryStatus permits
        DeliveryStatus.Pending,
        DeliveryStatus.Assigned,
        DeliveryStatus.PickedUp,
        DeliveryStatus.InTransit,
        DeliveryStatus.Delivered,
        DeliveryStatus.Failed {

    String name();

    boolean canTransitionTo(DeliveryStatus target);

    record Pending() implements DeliveryStatus {
        @Override
        public String name() {
            return "PENDING";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target instanceof Assigned || target instanceof Failed;
        }
    }

    record Assigned() implements DeliveryStatus {
        @Override
        public String name() {
            return "ASSIGNED";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target instanceof PickedUp || target instanceof Failed;
        }
    }

    record PickedUp() implements DeliveryStatus {
        @Override
        public String name() {
            return "PICKED_UP";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target instanceof InTransit || target instanceof Failed;
        }
    }

    record InTransit() implements DeliveryStatus {
        @Override
        public String name() {
            return "IN_TRANSIT";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return target instanceof Delivered || target instanceof Failed;
        }
    }

    record Delivered() implements DeliveryStatus {
        @Override
        public String name() {
            return "DELIVERED";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return false;
        }
    }

    record Failed() implements DeliveryStatus {
        @Override
        public String name() {
            return "FAILED";
        }

        @Override
        public boolean canTransitionTo(DeliveryStatus target) {
            return false;
        }
    }

    static DeliveryStatus fromString(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> new Pending();
            case "ASSIGNED" -> new Assigned();
            case "PICKED_UP" -> new PickedUp();
            case "IN_TRANSIT" -> new InTransit();
            case "DELIVERED" -> new Delivered();
            case "FAILED" -> new Failed();
            default -> throw new IllegalArgumentException("Unknown delivery status: " + status);
        };
    }
}
