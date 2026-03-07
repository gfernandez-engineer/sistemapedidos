package com.food.ordering.payments.domain.model;

import java.util.Set;

public sealed interface PaymentStatus permits
        PaymentStatus.Pending,
        PaymentStatus.Processing,
        PaymentStatus.Completed,
        PaymentStatus.Failed,
        PaymentStatus.Refunded {

    String name();

    boolean canTransitionTo(PaymentStatus target);

    record Pending() implements PaymentStatus {
        @Override
        public String name() {
            return "PENDING";
        }

        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target instanceof Processing || target instanceof Failed;
        }
    }

    record Processing() implements PaymentStatus {
        @Override
        public String name() {
            return "PROCESSING";
        }

        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target instanceof Completed || target instanceof Failed;
        }
    }

    record Completed() implements PaymentStatus {
        @Override
        public String name() {
            return "COMPLETED";
        }

        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target instanceof Refunded;
        }
    }

    record Failed() implements PaymentStatus {
        @Override
        public String name() {
            return "FAILED";
        }

        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target instanceof Pending;
        }
    }

    record Refunded() implements PaymentStatus {
        @Override
        public String name() {
            return "REFUNDED";
        }

        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return false;
        }
    }

    static PaymentStatus fromString(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> new Pending();
            case "PROCESSING" -> new Processing();
            case "COMPLETED" -> new Completed();
            case "FAILED" -> new Failed();
            case "REFUNDED" -> new Refunded();
            default -> throw new IllegalArgumentException("Unknown payment status: " + status);
        };
    }
}
