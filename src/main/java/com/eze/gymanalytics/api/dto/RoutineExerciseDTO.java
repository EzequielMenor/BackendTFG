package com.eze.gymanalytics.api.dto;

public class RoutineExerciseDTO {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private String muscleGroup;
    private String thumbnailUrl;
    private Integer exerciseOrder;
    private Integer targetSeries;
    private String notes;

    public RoutineExerciseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }

    public Integer getTargetSeries() { return targetSeries; }
    public void setTargetSeries(Integer targetSeries) { this.targetSeries = targetSeries; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
