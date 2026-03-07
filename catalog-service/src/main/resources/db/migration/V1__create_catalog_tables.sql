CREATE TABLE restaurants (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    address      VARCHAR(255) NOT NULL,
    phone        VARCHAR(255) NOT NULL,
    cuisine_type VARCHAR(255) NOT NULL,
    rating       NUMERIC(3,2),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);

CREATE TABLE products (
    id            BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT       NOT NULL REFERENCES restaurants(id),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    price         NUMERIC(10,2) NOT NULL,
    category      VARCHAR(255) NOT NULL,
    image_url     VARCHAR(500),
    available     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);

CREATE INDEX idx_products_restaurant ON products(restaurant_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_restaurants_cuisine ON restaurants(cuisine_type);
CREATE INDEX idx_restaurants_active ON restaurants(active);
