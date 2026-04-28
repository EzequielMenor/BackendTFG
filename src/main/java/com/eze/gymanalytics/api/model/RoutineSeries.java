package com.eze.gymanalytics.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "routine_series")
public class RoutineSeries {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routine_exercise_id", nullable = false)
  private RoutineExercise routineExercise;

  @Column(name = "set_order", nullable = false)
  private Integer setOrder;

  @Column(name = "target_weight", precision = 6, scale = 2)
  private BigDecimal targetWeight;

  @Column(name = "target_reps_min")
  private Integer targetRepsMin;

  @Column(name = "target_reps_max")
  private Integer targetRepsMax;

  public RoutineSeries() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public RoutineExercise getRoutineExercise() { return routineExercise; }
  public void setRoutineExercise(RoutineExercise routineExercise) { this.routineExercise = routineExercise; }

  public Integer getSetOrder() { return setOrder; }
  public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

  public BigDecimal getTargetWeight() { return targetWeight; }
  public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

  public Integer getTargetRepsMin() { return targetRepsMin; }
  public void setTargetRepsMin(Integer targetRepsMin) { this.targetRepsMin = targetRepsMin; }

  public Integer getTargetRepsMax() { return targetRepsMax; }
  public void setTargetRepsMax(Integer targetRepsMax) { this.targetRepsMax = targetRepsMax; }
}
