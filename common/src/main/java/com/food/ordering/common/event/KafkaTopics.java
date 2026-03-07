package com.food.ordering.common.event;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_STATUS_CHANGED = "order.status.changed";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String DELIVERY_ASSIGNED = "delivery.assigned";
    public static final String DELIVERY_STATUS_CHANGED = "delivery.status.changed";
}
