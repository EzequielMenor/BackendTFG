package com.eze.gymanalytics.api.dto.routines;

import java.util.List;

public class RoutineDTO {
  private Long id;
  private String name;
  private String description;
  private List<RoutineExerciseDTO> exercises;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public List<RoutineExerciseDTO> getExercises() {
    return exercises;
  }

  public void setExercises(List<RoutineExerciseDTO> exercises) {
    this.exercises = exercises;
  }
}
