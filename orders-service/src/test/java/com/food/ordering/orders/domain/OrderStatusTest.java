package com.food.ordering.orders.domain;

import com.food.ordering.orders.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Nested
    @DisplayName("Valid transitions")
    class ValidTransitions {

        @Test
        @DisplayName("PENDING can transition to CONFIRMED")
        void pendingToConfirmed() {
            OrderStatus pending = new OrderStatus.Pending();
            OrderStatus confirmed = new OrderStatus.Confirmed();
            assertTrue(pending.canTransitionTo(confirmed));
        }

        @Test
        @DisplayName("PENDING can transition to CANCELLED")
        void pendingToCancelled() {
            OrderStatus pending = new OrderStatus.Pending();
            OrderStatus cancelled = new OrderStatus.Cancelled();
            assertTrue(pending.canTransitionTo(cancelled));
        }

        @Test
        @DisplayName("CONFIRMED can transition to PREPARING")
        void confirmedToPreparing() {
            OrderStatus confirmed = new OrderStatus.Confirmed();
            OrderStatus preparing = new OrderStatus.Preparing();
            assertTrue(confirmed.canTransitionTo(preparing));
        }

        @Test
        @DisplayName("CONFIRMED can transition to CANCELLED")
        void confirmedToCancelled() {
            OrderStatus confirmed = new OrderStatus.Confirmed();
            OrderStatus cancelled = new OrderStatus.Cancelled();
            assertTrue(confirmed.canTransitionTo(cancelled));
        }

        @Test
        @DisplayName("PREPARING can transition to READY")
        void preparingToReady() {
            OrderStatus preparing = new OrderStatus.Preparing();
            OrderStatus ready = new OrderStatus.Ready();
            assertTrue(preparing.canTransitionTo(ready));
        }

        @Test
        @DisplayName("READY can transition to IN_DELIVERY")
        void readyToInDelivery() {
            OrderStatus ready = new OrderStatus.Ready();
            OrderStatus inDelivery = new OrderStatus.InDelivery();
            assertTrue(ready.canTransitionTo(inDelivery));
        }

        @Test
        @DisplayName("IN_DELIVERY can transition to DELIVERED")
        void inDeliveryToDelivered() {
            OrderStatus inDelivery = new OrderStatus.InDelivery();
            OrderStatus delivered = new OrderStatus.Delivered();
            assertTrue(inDelivery.canTransitionTo(delivered));
        }
    }

    @Nested
    @DisplayName("Invalid transitions")
    class InvalidTransitions {

        @Test
        @DisplayName("PENDING cannot transition to DELIVERED")
        void pendingToDelivered() {
            OrderStatus pending = new OrderStatus.Pending();
            OrderStatus delivered = new OrderStatus.Delivered();
            assertFalse(pending.canTransitionTo(delivered));
        }

        @Test
        @DisplayName("DELIVERED cannot transition to any state")
        void deliveredToAny() {
            OrderStatus delivered = new OrderStatus.Delivered();
            assertFalse(delivered.canTransitionTo(new OrderStatus.Pending()));
            assertFalse(delivered.canTransitionTo(new OrderStatus.Cancelled()));
            assertFalse(delivered.canTransitionTo(new OrderStatus.Confirmed()));
        }

        @Test
        @DisplayName("CANCELLED cannot transition to any state")
        void cancelledToAny() {
            OrderStatus cancelled = new OrderStatus.Cancelled();
            assertFalse(cancelled.canTransitionTo(new OrderStatus.Pending()));
            assertFalse(cancelled.canTransitionTo(new OrderStatus.Confirmed()));
            assertFalse(cancelled.canTransitionTo(new OrderStatus.Delivered()));
        }

        @Test
        @DisplayName("IN_DELIVERY cannot transition to CANCELLED")
        void inDeliveryToCancelled() {
            OrderStatus inDelivery = new OrderStatus.InDelivery();
            OrderStatus cancelled = new OrderStatus.Cancelled();
            assertFalse(inDelivery.canTransitionTo(cancelled));
        }

        @Test
        @DisplayName("CONFIRMED cannot transition to DELIVERED directly")
        void confirmedToDelivered() {
            OrderStatus confirmed = new OrderStatus.Confirmed();
            OrderStatus delivered = new OrderStatus.Delivered();
            assertFalse(confirmed.canTransitionTo(delivered));
        }
    }

    @Nested
    @DisplayName("Status name and fromString")
    class StatusNameAndFromString {

        @Test
        @DisplayName("name() returns correct string")
        void nameReturnsCorrectString() {
            assertEquals("PENDING", new OrderStatus.Pending().name());
            assertEquals("CONFIRMED", new OrderStatus.Confirmed().name());
            assertEquals("PREPARING", new OrderStatus.Preparing().name());
            assertEquals("READY", new OrderStatus.Ready().name());
            assertEquals("IN_DELIVERY", new OrderStatus.InDelivery().name());
            assertEquals("DELIVERED", new OrderStatus.Delivered().name());
            assertEquals("CANCELLED", new OrderStatus.Cancelled().name());
        }

        @Test
        @DisplayName("fromString() creates correct status")
        void fromStringCreatesCorrectStatus() {
            assertInstanceOf(OrderStatus.Pending.class, OrderStatus.fromString("PENDING"));
            assertInstanceOf(OrderStatus.Confirmed.class, OrderStatus.fromString("CONFIRMED"));
            assertInstanceOf(OrderStatus.InDelivery.class, OrderStatus.fromString("IN_DELIVERY"));
        }

        @Test
        @DisplayName("fromString() throws for unknown status")
        void fromStringThrowsForUnknown() {
            assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromString("UNKNOWN"));
        }
    }
}
