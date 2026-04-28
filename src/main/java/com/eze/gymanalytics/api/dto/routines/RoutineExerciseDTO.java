package com.eze.gymanalytics.api.dto.routines;

import java.util.List;

public class RoutineExerciseDTO {
  private Long id;
  private Long exerciseId;
  private Integer exerciseOrder;
  private String exerciseName;
  private String muscleGroup;
  private String thumbnailUrl;
  private String notes;
  private Integer targetSeries;
  private List<RoutineSeriesDTO> series;

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

  public String getExerciseName() {
    return exerciseName;
  }

  public void setExerciseName(String exerciseName) {
    this.exerciseName = exerciseName;
  }

  public String getMuscleGroup() {
    return muscleGroup;
  }

  public void setMuscleGroup(String muscleGroup) {
    this.muscleGroup = muscleGroup;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Integer getTargetSeries() {
    return targetSeries;
  }

  public void setTargetSeries(Integer targetSeries) {
    this.targetSeries = targetSeries;
  }

  public List<RoutineSeriesDTO> getSeries() { return series; }
  public void setSeries(List<RoutineSeriesDTO> series) { this.series = series; }
}
