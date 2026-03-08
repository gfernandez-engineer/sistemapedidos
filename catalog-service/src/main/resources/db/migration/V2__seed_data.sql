-- Seed data for E2E testing
INSERT INTO restaurants (name, description, address, phone, cuisine_type, rating, active, created_at, updated_at)
VALUES
  ('Sushi Master', 'Autentica cocina japonesa con ingredientes frescos', 'Av. Isidora Goyenechea 3000, Las Condes', '+56223456789', 'JAPANESE', 4.5, true, NOW(), NOW()),
  ('La Pizzeria Italiana', 'Las mejores pizzas artesanales de Santiago', 'Av. Italia 1200, Providencia', '+56224567890', 'ITALIAN', 4.2, true, NOW(), NOW()),
  ('El Rincón Mexicano', 'Sabores autenticos de Mexico', 'Av. Manuel Montt 800, Providencia', '+56225678901', 'MEXICAN', 4.0, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO products (restaurant_id, name, description, price, category, available, created_at, updated_at)
VALUES
  (1, 'Salmon Roll Premium x8', 'Rolls de salmon premium con palta y queso crema', 14990.00, 'SUSHI', true, NOW(), NOW()),
  (1, 'Tempura Mixto', 'Variedad de tempura de camarones y vegetales', 12990.00, 'APPETIZER', true, NOW(), NOW()),
  (1, 'Ramen Tonkotsu', 'Ramen con caldo de cerdo, huevo y chashu', 11990.00, 'SOUP', true, NOW(), NOW()),
  (2, 'Pizza Margherita', 'Tomate, mozzarella fresca y albahaca', 9990.00, 'PIZZA', true, NOW(), NOW()),
  (2, 'Pizza Pepperoni', 'Pepperoni, mozzarella y salsa de tomate', 11990.00, 'PIZZA', true, NOW(), NOW()),
  (3, 'Tacos al Pastor x3', 'Tacos con carne al pastor, piña y cilantro', 8990.00, 'TACOS', true, NOW(), NOW()),
  (3, 'Burrito Supreme', 'Burrito grande con carne, frijoles, arroz y guacamole', 10990.00, 'BURRITO', true, NOW(), NOW())
ON CONFLICT DO NOTHING;
