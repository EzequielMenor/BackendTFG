-- Usuario de prueba para el importador
INSERT INTO profiles (id, email, username, role, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'eze@test.com', 'eze_gym', 'user', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
