-- Seed data for E2E testing
INSERT INTO drivers (name, phone, vehicle_type, available, current_location)
VALUES
  ('Carlos Rodriguez', '+56911223344', 'MOTORCYCLE', true, 'Santiago Centro'),
  ('Ana Martinez', '+56922334455', 'CAR', true, 'Las Condes'),
  ('Pedro Gonzalez', '+56933445566', 'BICYCLE', true, 'Providencia')
ON CONFLICT DO NOTHING;
