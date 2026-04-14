package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.*;
import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.model.Workout;
import com.eze.gymanalytics.api.model.WorkoutExercise;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ProfileRepository profileRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public AdminService(ProfileRepository profileRepository,
                        WorkoutRepository workoutRepository,
                        ExerciseRepository exerciseRepository) {
        this.profileRepository = profileRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // -------------------------------------------------------------------------
    // Stats + Users (existing)
    // -------------------------------------------------------------------------

    public AdminStatsDTO getStats() {
        long totalUsers = profileRepository.count();
        long totalWorkouts = workoutRepository.count();
        long totalExercises = exerciseRepository.count();
        long activeLastWeek = workoutRepository.countDistinctActiveUsersSince(
                OffsetDateTime.now().minusDays(7));

        return new AdminStatsDTO(totalUsers, totalWorkouts, totalExercises, activeLastWeek);
    }

    public List<UserProfileDTO> getUsers() {
        return profileRepository.findAll()
                .stream()
                .map(profile -> new UserProfileDTO(
                        profile.getId(),
                        profile.getEmail(),
                        profile.getUsername(),
                        profile.getRole(),
                        profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Workouts CRUD
    // -------------------------------------------------------------------------

    /**
     * Devuelve todos los workouts de la plataforma ordenados por fecha desc.
     */
    public List<AdminWorkoutDTO> getAllWorkouts() {
        return workoutRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .map(this::toAdminWorkoutDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el detalle de un workout incluyendo sus ejercicios.
     * Lanza 404 si no existe.
     */
    public AdminWorkoutDetailDTO getWorkoutById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Workout no encontrado: " + id));
        return toAdminWorkoutDetailDTO(workout);
    }

    /**
     * Actualiza los campos editables de un workout (name + notes).
     * El admin no puede cambiar el usuario propietario ni los tiempos.
     * Lanza 404 si no existe.
     */
    public AdminWorkoutDTO updateWorkout(Long id, String name, String notes) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Workout no encontrado: " + id));

        if (name != null) workout.setName(name);
        if (notes != null) workout.setNotes(notes);

        Workout saved = workoutRepository.save(workout);
        return toAdminWorkoutDTO(saved);
    }

    /**
     * Elimina un workout por id.
     * Lanza 404 si no existe.
     */
    public void deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Workout no encontrado: " + id);
        }
        workoutRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Exercises CRUD
    // -------------------------------------------------------------------------

    /**
     * Devuelve todos los ejercicios del catálogo ordenados por nombre.
     * El ordenamiento se delega al repositorio (ORDER BY name ASC en BD)
     * en lugar de hacerlo en memoria con un Comparator.
     */
    public List<AdminExerciseDTO> getAllExercises() {
        return exerciseRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toAdminExerciseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el detalle de un ejercicio por id.
     * Lanza 404 si no existe.
     */
    public AdminExerciseDTO getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ejercicio no encontrado: " + id));
        return toAdminExerciseDTO(exercise);
    }

    /**
     * Crea un nuevo ejercicio en el catálogo.
     * Valida que el nombre sea único (case-insensitive).
     * Lanza 409 si ya existe un ejercicio con ese nombre.
     */
    public AdminExerciseDTO createExercise(Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        exerciseRepository.findByNameIgnoreCase(name.trim()).ifPresent(e -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un ejercicio con el nombre: " + name);
        });

        Exercise exercise = new Exercise();
        applyExerciseFields(exercise, body);

        Exercise saved = exerciseRepository.save(exercise);
        return toAdminExerciseDTO(saved);
    }

    /**
     * Actualiza un ejercicio existente.
     * Valida unicidad del nombre excluyendo el propio ejercicio.
     * Lanza 404 si no existe, 409 si el nombre ya lo usa otro ejercicio.
     */
    public AdminExerciseDTO updateExercise(Long id, Map<String, Object> body) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ejercicio no encontrado: " + id));

        String name = (String) body.get("name");
        if (name != null && !name.isBlank()) {
            exerciseRepository.findByNameIgnoreCaseAndIdNot(name.trim(), id).ifPresent(e -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe un ejercicio con el nombre: " + name);
            });
        }

        applyExerciseFields(exercise, body);
        Exercise saved = exerciseRepository.save(exercise);
        return toAdminExerciseDTO(saved);
    }

    /**
     * Elimina un ejercicio del catálogo.
     * Lanza 404 si no existe.
     */
    public void deleteExercise(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ejercicio no encontrado: " + id);
        }
        exerciseRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Helpers — Exercises
    // -------------------------------------------------------------------------

    /**
     * Aplica los campos del body al objeto Exercise.
     * Solo actualiza los campos presentes en el body (no null).
     */
    @SuppressWarnings("unchecked")
    private void applyExerciseFields(Exercise exercise, Map<String, Object> body) {
        if (body.containsKey("name")) {
            String n = (String) body.get("name");
            if (n != null) exercise.setName(n.trim());
        }
        if (body.containsKey("muscleGroup"))      exercise.setMuscleGroup((String) body.get("muscleGroup"));
        if (body.containsKey("description"))      exercise.setDescription((String) body.get("description"));
        if (body.containsKey("imageUrl"))         exercise.setImageUrl((String) body.get("imageUrl"));
        if (body.containsKey("videoUrl"))         exercise.setVideoUrl((String) body.get("videoUrl"));
        if (body.containsKey("thumbnailUrl"))     exercise.setThumbnailUrl((String) body.get("thumbnailUrl"));
        if (body.containsKey("equipment"))        exercise.setEquipment((String) body.get("equipment"));
        if (body.containsKey("secondaryMuscles")) exercise.setSecondaryMuscles((String) body.get("secondaryMuscles"));

        // aliases puede venir como List<String> o como String separado por comas
        if (body.containsKey("aliases")) {
            Object aliasesRaw = body.get("aliases");
            if (aliasesRaw instanceof List) {
                exercise.setAliases((List<String>) aliasesRaw);
            } else if (aliasesRaw instanceof String) {
                String s = (String) aliasesRaw;
                if (s.isBlank()) {
                    exercise.setAliases(List.of());
                } else {
                    exercise.setAliases(Arrays.stream(s.split(","))
                            .map(String::trim)
                            .filter(x -> !x.isEmpty())
                            .collect(Collectors.toList()));
                }
            }
        }
    }

    private AdminExerciseDTO toAdminExerciseDTO(Exercise e) {
        return new AdminExerciseDTO(
                e.getId(),
                e.getName(),
                e.getMuscleGroup(),
                e.getDescription(),
                e.getImageUrl(),
                e.getVideoUrl(),
                e.getThumbnailUrl(),
                e.getEquipment(),
                e.getSecondaryMuscles(),
                e.getAliases() != null ? e.getAliases() : List.of(),
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : null
        );
    }

    // -------------------------------------------------------------------------
    // Mappers — Workouts
    // -------------------------------------------------------------------------

    private AdminWorkoutDTO toAdminWorkoutDTO(Workout w) {
        String userEmail = w.getUser() != null ? w.getUser().getEmail() : null;
        int exerciseCount = w.getWorkoutExercises() != null ? w.getWorkoutExercises().size() : 0;
        return new AdminWorkoutDTO(
                w.getId(),
                w.getName(),
                userEmail,
                w.getStartTime() != null ? w.getStartTime().toString() : null,
                w.getEndTime() != null ? w.getEndTime().toString() : null,
                w.getNotes(),
                w.getTotalVolume(),
                exerciseCount
        );
    }

    private AdminWorkoutDetailDTO toAdminWorkoutDetailDTO(Workout w) {
        List<WorkoutExerciseDTO> exercises = w.getWorkoutExercises() == null
                ? List.of()
                : w.getWorkoutExercises().stream()
                        .map(this::toWorkoutExerciseDTO)
                        .collect(Collectors.toList());

        String userEmail = w.getUser() != null ? w.getUser().getEmail() : null;
        return new AdminWorkoutDetailDTO(
                w.getId(),
                w.getName(),
                userEmail,
                w.getStartTime() != null ? w.getStartTime().toString() : null,
                w.getEndTime() != null ? w.getEndTime().toString() : null,
                w.getNotes(),
                w.getTotalVolume(),
                exercises.size(),
                exercises
        );
    }

    private WorkoutExerciseDTO toWorkoutExerciseDTO(WorkoutExercise we) {
        WorkoutExerciseDTO dto = new WorkoutExerciseDTO();
        dto.setId(we.getId());
        dto.setExerciseOrder(we.getExerciseOrder());
        dto.setNotes(we.getNotes());

        if (we.getExercise() != null) {
            ExerciseInfoDTO info = new ExerciseInfoDTO();
            info.setName(we.getExercise().getName());
            info.setMuscleGroup(we.getExercise().getMuscleGroup());
            dto.setExercise(info);
        }

        if (we.getSeries() != null) {
            List<SerieDTO> series = we.getSeries().stream().map(s -> {
                SerieDTO sd = new SerieDTO();
                sd.setId(s.getId());
                sd.setWeight(s.getWeight());
                sd.setReps(s.getReps());
                sd.setRpe(s.getRpe());
                sd.setIsWarmup(s.getIsWarmup());
                sd.setSetOrder(s.getSetOrder());
                return sd;
            }).collect(Collectors.toList());
            dto.setSeries(series);
        }

        return dto;
    }
}
