package com.food.ordering.users.domain.model;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public sealed interface UserRole permits UserRole.Customer, UserRole.RestaurantOwner, UserRole.DeliveryDriver {

        String name();

        record Customer() implements UserRole {
            @Override
            public String name() {
                return "CUSTOMER";
            }
        }

        record RestaurantOwner() implements UserRole {
            @Override
            public String name() {
                return "RESTAURANT_OWNER";
            }
        }

        record DeliveryDriver() implements UserRole {
            @Override
            public String name() {
                return "DELIVERY_DRIVER";
            }
        }
    }

    public User() {
    }

    public User(Long id, String email, String password, String firstName, String lastName,
                String phone, String address, UserRole role, boolean active,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
