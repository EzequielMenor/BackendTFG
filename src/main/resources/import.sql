-- Usuario de prueba para el importador
INSERT INTO profiles (id, email, username, role, created_at) VALUES ('550e8400-e29b-41d4-a716-446655440000', 'eze@test.com', 'eze_gym', 'user', CURRENT_TIMESTAMP);

-- Ejercicios base — incluye todos los campos del esquema completo:
--   equipment, secondary_muscles, thumbnail_url, video_url, aliases
INSERT INTO exercises (name, muscle_group, description, equipment, secondary_muscles, thumbnail_url, video_url, aliases)
  VALUES ('Press Banca', 'Pecho', 'Ejercicio básico para pecho', 'Barra', 'Tríceps, Hombro anterior', NULL, NULL, '{}');

INSERT INTO exercises (name, muscle_group, description, equipment, secondary_muscles, thumbnail_url, video_url, aliases)
  VALUES ('Sentadilla', 'Piernas', 'Ejercicio compuesto principal', 'Barra', 'Glúteos, Core', NULL, NULL, '{"Squat","Back Squat"}');

INSERT INTO exercises (name, muscle_group, description, equipment, secondary_muscles, thumbnail_url, video_url, aliases)
  VALUES ('Peso Muerto', 'Espalda', 'Ejercicio de fuerza posterior', 'Barra', 'Glúteos, Isquiotibiales', NULL, NULL, '{"Deadlift","DL"}');

INSERT INTO exercises (name, muscle_group, description, equipment, secondary_muscles, thumbnail_url, video_url, aliases)
  VALUES ('Dominadas', 'Espalda', 'Ejercicio de tracción vertical', 'Barra fija', 'Bíceps', NULL, NULL, '{"Pull-up","Chin-up"}');

INSERT INTO exercises (name, muscle_group, description, equipment, secondary_muscles, thumbnail_url, video_url, aliases)
  VALUES ('Flexiones', 'Pecho', 'Ejercicio de empuje corporal', 'Peso corporal', 'Tríceps, Core', NULL, NULL, '{"Push-up"}');
