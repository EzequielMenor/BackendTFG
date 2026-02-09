package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
