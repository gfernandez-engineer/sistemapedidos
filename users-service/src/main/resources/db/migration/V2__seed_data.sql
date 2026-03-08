-- Seed data for E2E testing
INSERT INTO users (email, password, first_name, last_name, phone, address, role, active, created_at, updated_at)
VALUES
  ('admin@foodordering.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'System', '+56900000001', 'Admin Office', 'ADMIN', true, NOW(), NOW()),
  ('juan.perez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Juan', 'Perez', '+56912345678', 'Av. Providencia 1234, Santiago', 'CUSTOMER', true, NOW(), NOW()),
  ('maria.restaurant@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maria', 'Lopez', '+56987654321', 'Av. Las Condes 5678', 'RESTAURANT_OWNER', true, NOW(), NOW()),
  ('carlos.driver@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carlos', 'Rodriguez', '+56911223344', 'Av. Apoquindo 900', 'DELIVERY_DRIVER', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
