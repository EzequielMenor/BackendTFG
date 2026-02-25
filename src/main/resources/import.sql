-- Usuario de prueba para el importador
INSERT INTO profiles (id, email, username, role, created_at) 
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'eze@test.com', 'eze_gym', 'user', CURRENT_TIMESTAMP);

-- Ejercicios base
INSERT INTO exercises (name, muscle_group, description) VALUES ('Press Banca', 'Pecho', 'Ejercicio básico para pecho');
INSERT INTO exercises (name, muscle_group, description) VALUES ('Sentadilla', 'Piernas', 'Ejercicio compuesto principal');
INSERT INTO exercises (name, muscle_group, description) VALUES ('Peso Muerto', 'Espalda', 'Ejercicio de fuerza posterior');
INSERT INTO exercises (name, muscle_group, description) VALUES ('Dominadas', 'Espalda', 'Ejercicio de tracción vertical');
INSERT INTO exercises (name, muscle_group, description) VALUES ('Flexiones', 'Pecho', 'Ejercicio de empuje corporal');
