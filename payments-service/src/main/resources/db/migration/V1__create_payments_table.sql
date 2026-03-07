CREATE TABLE payments (
    id             BIGSERIAL PRIMARY KEY,
    order_id       VARCHAR(255)  NOT NULL,
    user_id        VARCHAR(255)  NOT NULL,
    amount         NUMERIC(10,2) NOT NULL,
    payment_method VARCHAR(50)   NOT NULL,
    status         VARCHAR(50)   NOT NULL,
    transaction_id VARCHAR(255)  UNIQUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
