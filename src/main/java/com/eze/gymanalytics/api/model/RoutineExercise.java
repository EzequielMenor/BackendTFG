package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routine_exercises")
@Getter
@Setter
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
}
