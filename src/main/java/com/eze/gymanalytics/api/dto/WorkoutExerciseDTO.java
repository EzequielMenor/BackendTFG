package com.eze.gymanalytics.api.dto;

import java.util.List;

public class WorkoutExerciseDTO {
    private Long id;
    private Long exerciseId; // Para crear workouts (input)
    private Integer exerciseOrder;
    private String notes;
    private ExerciseInfoDTO exercise; // Objeto anidado para leer (output)
    private List<SerieDTO> series;

    public WorkoutExerciseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getExerciseOrder() {
        return exerciseOrder;
    }

    public void setExerciseOrder(Integer exerciseOrder) {
        this.exerciseOrder = exerciseOrder;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ExerciseInfoDTO getExercise() {
        return exercise;
    }

    public void setExercise(ExerciseInfoDTO exercise) {
        this.exercise = exercise;
    }

    public List<SerieDTO> getSeries() {
        return series;
    }

    public void setSeries(List<SerieDTO> series) {
        this.series = series;
    }
}