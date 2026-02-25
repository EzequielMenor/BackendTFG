package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_id", nullable = false)
  private Workout workout;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(name = "exercise_order", nullable = false)
  private Integer exerciseOrder;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Serie> series;

  public WorkoutExercise() {}

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Workout getWorkout() { return workout; }
  public void setWorkout(Workout workout) { this.workout = workout; }

  public Exercise getExercise() { return exercise; }
  public void setExercise(Exercise exercise) { this.exercise = exercise; }

  public Integer getExerciseOrder() { return exerciseOrder; }
  public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public List<Serie> getSeries() { return series; }
  public void setSeries(List<Serie> series) { this.series = series; }
}
