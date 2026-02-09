package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
