CREATE TABLE drivers (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    phone            VARCHAR(255) NOT NULL,
    vehicle_type     VARCHAR(100) NOT NULL,
    available        BOOLEAN      NOT NULL DEFAULT TRUE,
    current_location VARCHAR(255)
);

CREATE TABLE deliveries (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                VARCHAR(255) NOT NULL UNIQUE,
    driver_id               BIGINT       REFERENCES drivers(id),
    delivery_address        VARCHAR(500) NOT NULL,
    status                  VARCHAR(50)  NOT NULL,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    actual_delivery_time    TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_deliveries_order ON deliveries(order_id);
CREATE INDEX idx_deliveries_driver ON deliveries(driver_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_drivers_available ON drivers(available);
