package com.eze.gymanalytics.api.dto.routines;

import java.util.List;

public class RoutineCreateRequest {
  private String name;
  private String description;
  private List<RoutineExerciseCreateRequest> exercises;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<RoutineExerciseCreateRequest> getExercises() {
    return exercises;
  }

  public void setExercises(List<RoutineExerciseCreateRequest> exercises) {
    this.exercises = exercises;
  }
}
