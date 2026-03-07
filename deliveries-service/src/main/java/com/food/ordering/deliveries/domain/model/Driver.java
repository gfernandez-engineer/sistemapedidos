package com.food.ordering.deliveries.domain.model;

public class Driver {

    private Long id;
    private String name;
    private String phone;
    private String vehicleType;
    private boolean available;
    private String currentLocation;

    public Driver() {
    }

    public Driver(Long id, String name, String phone, String vehicleType,
                  boolean available, String currentLocation) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.available = available;
        this.currentLocation = currentLocation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
}
