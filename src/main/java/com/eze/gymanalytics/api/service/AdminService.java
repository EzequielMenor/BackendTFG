package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.*;
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
import java.util.List;
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
    // Mappers
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
