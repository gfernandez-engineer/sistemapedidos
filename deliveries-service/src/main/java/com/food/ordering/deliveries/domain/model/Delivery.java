package com.food.ordering.deliveries.domain.model;

import java.time.Instant;

public class Delivery {

    private Long id;
    private String orderId;
    private Long driverId;
    private String deliveryAddress;
    private DeliveryStatus status;
    private Instant estimatedDeliveryTime;
    private Instant actualDeliveryTime;
    private Instant createdAt;
    private Instant updatedAt;

    public Delivery() {
    }

    public Delivery(Long id, String orderId, Long driverId, String deliveryAddress,
                    DeliveryStatus status, Instant estimatedDeliveryTime,
                    Instant actualDeliveryTime, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.driverId = driverId;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public Instant getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Instant estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public Instant getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(Instant actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
