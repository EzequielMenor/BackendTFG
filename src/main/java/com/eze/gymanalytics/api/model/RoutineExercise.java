package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routine_exercises")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineExercise {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routine_id", nullable = false)
  private Routine routine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(name = "exercise_order", nullable = false)
  private Integer exerciseOrder;

  @Column(name = "target_series")
  private Integer targetSeries;

  @Column(columnDefinition = "TEXT")
  private String notes;

  // Manual getters/setters to avoid relying on Lombok processing
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Routine getRoutine() {
    return routine;
  }

  public void setRoutine(Routine routine) {
    this.routine = routine;
  }

  public Exercise getExercise() {
    return exercise;
  }

  public void setExercise(Exercise exercise) {
    this.exercise = exercise;
  }

  public Integer getExerciseOrder() {
    return exerciseOrder;
  }

  public void setExerciseOrder(Integer exerciseOrder) {
    this.exerciseOrder = exerciseOrder;
  }

  public Integer getTargetSeries() {
    return targetSeries;
  }

  public void setTargetSeries(Integer targetSeries) {
    this.targetSeries = targetSeries;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
