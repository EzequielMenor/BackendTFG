package com.eze.gymanalytics.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de detalle de un entrenamiento para el panel de administración.
 * Extiende AdminWorkoutDTO añadiendo la lista de ejercicios.
 */
public class AdminWorkoutDetailDTO extends AdminWorkoutDTO {

    private List<WorkoutExerciseDTO> exercises;

    public AdminWorkoutDetailDTO() {}

    public AdminWorkoutDetailDTO(Long id, String name, String userEmail,
                                 String startTime, String endTime,
                                 String notes, BigDecimal totalVolume,
                                 int exerciseCount,
                                 List<WorkoutExerciseDTO> exercises) {
        super(id, name, userEmail, startTime, endTime, notes, totalVolume, exerciseCount);
        this.exercises = exercises;
    }

    public List<WorkoutExerciseDTO> getExercises() { return exercises; }
    public void setExercises(List<WorkoutExerciseDTO> exercises) { this.exercises = exercises; }
}
