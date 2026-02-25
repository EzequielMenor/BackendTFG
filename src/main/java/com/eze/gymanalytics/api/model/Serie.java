package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "series")
public class Serie {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_exercise_id", nullable = false)
  private WorkoutExercise workoutExercise;

  @Column(nullable = false)
  private BigDecimal weight;

  @Column(nullable = false)
  private Integer reps;

  private BigDecimal rpe;

  @Column(name = "is_warmup")
  private Boolean isWarmup = false;

  @Column(name = "set_order", nullable = false)
  private Integer setOrder;

  public Serie() {
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public WorkoutExercise getWorkoutExercise() {
    return workoutExercise;
  }

  public void setWorkoutExercise(WorkoutExercise workoutExercise) {
    this.workoutExercise = workoutExercise;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public Integer getReps() {
    return reps;
  }

  public void setReps(Integer reps) {
    this.reps = reps;
  }

  public BigDecimal getRpe() {
    return rpe;
  }

  public void setRpe(BigDecimal rpe) {
    this.rpe = rpe;
  }

  public Boolean getIsWarmup() {
    return isWarmup;
  }

  public void setIsWarmup(Boolean isWarmup) {
    this.isWarmup = isWarmup;
  }

  public Integer getSetOrder() {
    return setOrder;
  }

  public void setSetOrder(Integer setOrder) {
    this.setOrder = setOrder;
  }
}
