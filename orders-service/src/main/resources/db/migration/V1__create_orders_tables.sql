CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    restaurant_id    BIGINT        NOT NULL,
    status           VARCHAR(50)   NOT NULL,
    total_amount     NUMERIC(10,2) NOT NULL,
    delivery_address VARCHAR(500)  NOT NULL,
    notes            VARCHAR(1000),
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL,
    unit_price   NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
