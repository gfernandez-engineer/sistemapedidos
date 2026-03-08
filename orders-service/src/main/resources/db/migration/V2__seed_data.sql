-- Seed data for E2E testing
-- Orders will be created via E2E flow, this provides a baseline order
INSERT INTO orders (user_id, restaurant_id, status, total_amount, delivery_address, notes, created_at, updated_at)
VALUES
  (2, 1, 'PENDING', 29980.00, 'Av. Providencia 1234, Santiago', 'Pedido de prueba inicial', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price)
VALUES
  (1, 1, 'Salmon Roll Premium x8', 2, 14990.00)
ON CONFLICT DO NOTHING;
