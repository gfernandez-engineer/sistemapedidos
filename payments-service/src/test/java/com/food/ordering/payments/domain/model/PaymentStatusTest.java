package com.food.ordering.payments.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStatusTest {

    @Test
    @DisplayName("PENDING can transition to PROCESSING")
    void pendingCanTransitionToProcessing() {
        PaymentStatus pending = new PaymentStatus.Pending();
        assertThat(pending.canTransitionTo(new PaymentStatus.Processing())).isTrue();
    }

    @Test
    @DisplayName("PENDING can transition to FAILED")
    void pendingCanTransitionToFailed() {
        PaymentStatus pending = new PaymentStatus.Pending();
        assertThat(pending.canTransitionTo(new PaymentStatus.Failed())).isTrue();
    }

    @Test
    @DisplayName("PENDING cannot transition to COMPLETED")
    void pendingCannotTransitionToCompleted() {
        PaymentStatus pending = new PaymentStatus.Pending();
        assertThat(pending.canTransitionTo(new PaymentStatus.Completed())).isFalse();
    }

    @Test
    @DisplayName("PENDING cannot transition to REFUNDED")
    void pendingCannotTransitionToRefunded() {
        PaymentStatus pending = new PaymentStatus.Pending();
        assertThat(pending.canTransitionTo(new PaymentStatus.Refunded())).isFalse();
    }

    @Test
    @DisplayName("PROCESSING can transition to COMPLETED")
    void processingCanTransitionToCompleted() {
        PaymentStatus processing = new PaymentStatus.Processing();
        assertThat(processing.canTransitionTo(new PaymentStatus.Completed())).isTrue();
    }

    @Test
    @DisplayName("PROCESSING can transition to FAILED")
    void processingCanTransitionToFailed() {
        PaymentStatus processing = new PaymentStatus.Processing();
        assertThat(processing.canTransitionTo(new PaymentStatus.Failed())).isTrue();
    }

    @Test
    @DisplayName("PROCESSING cannot transition to PENDING")
    void processingCannotTransitionToPending() {
        PaymentStatus processing = new PaymentStatus.Processing();
        assertThat(processing.canTransitionTo(new PaymentStatus.Pending())).isFalse();
    }

    @Test
    @DisplayName("COMPLETED can transition to REFUNDED")
    void completedCanTransitionToRefunded() {
        PaymentStatus completed = new PaymentStatus.Completed();
        assertThat(completed.canTransitionTo(new PaymentStatus.Refunded())).isTrue();
    }

    @Test
    @DisplayName("COMPLETED cannot transition to FAILED")
    void completedCannotTransitionToFailed() {
        PaymentStatus completed = new PaymentStatus.Completed();
        assertThat(completed.canTransitionTo(new PaymentStatus.Failed())).isFalse();
    }

    @Test
    @DisplayName("FAILED can transition to PENDING (retry)")
    void failedCanTransitionToPending() {
        PaymentStatus failed = new PaymentStatus.Failed();
        assertThat(failed.canTransitionTo(new PaymentStatus.Pending())).isTrue();
    }

    @Test
    @DisplayName("FAILED cannot transition to COMPLETED")
    void failedCannotTransitionToCompleted() {
        PaymentStatus failed = new PaymentStatus.Failed();
        assertThat(failed.canTransitionTo(new PaymentStatus.Completed())).isFalse();
    }

    @Test
    @DisplayName("REFUNDED cannot transition to any state")
    void refundedCannotTransitionToAnyState() {
        PaymentStatus refunded = new PaymentStatus.Refunded();
        assertThat(refunded.canTransitionTo(new PaymentStatus.Pending())).isFalse();
        assertThat(refunded.canTransitionTo(new PaymentStatus.Processing())).isFalse();
        assertThat(refunded.canTransitionTo(new PaymentStatus.Completed())).isFalse();
        assertThat(refunded.canTransitionTo(new PaymentStatus.Failed())).isFalse();
    }

    @Test
    @DisplayName("fromString should parse valid status names")
    void fromStringShouldParseValidNames() {
        assertThat(PaymentStatus.fromString("PENDING")).isInstanceOf(PaymentStatus.Pending.class);
        assertThat(PaymentStatus.fromString("PROCESSING")).isInstanceOf(PaymentStatus.Processing.class);
        assertThat(PaymentStatus.fromString("COMPLETED")).isInstanceOf(PaymentStatus.Completed.class);
        assertThat(PaymentStatus.fromString("FAILED")).isInstanceOf(PaymentStatus.Failed.class);
        assertThat(PaymentStatus.fromString("REFUNDED")).isInstanceOf(PaymentStatus.Refunded.class);
    }

    @Test
    @DisplayName("fromString should throw for unknown status")
    void fromStringShouldThrowForUnknownStatus() {
        assertThatThrownBy(() -> PaymentStatus.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown payment status");
    }

    @Test
    @DisplayName("Payment domain model should enforce transition rules")
    void paymentShouldEnforceTransitionRules() {
        Payment payment = new Payment();
        payment.setStatus(new PaymentStatus.Pending());

        // Valid transition
        payment.transitionTo(new PaymentStatus.Processing());
        assertThat(payment.getStatus()).isInstanceOf(PaymentStatus.Processing.class);

        // Invalid transition from PROCESSING to PENDING
        assertThatThrownBy(() -> payment.transitionTo(new PaymentStatus.Pending()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition");
    }
}
