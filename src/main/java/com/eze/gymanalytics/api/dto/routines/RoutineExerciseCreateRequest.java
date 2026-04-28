package com.eze.gymanalytics.api.dto.routines;

import java.util.List;

public class RoutineExerciseCreateRequest {
  private Long exerciseId;
  private Integer exerciseOrder;
  private Integer targetSeries;
  private List<RoutineSeriesCreateRequest> series;

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

  public Integer getTargetSeries() {
    return targetSeries;
  }

  public void setTargetSeries(Integer targetSeries) {
    this.targetSeries = targetSeries;
  }

  public List<RoutineSeriesCreateRequest> getSeries() { return series; }
  public void setSeries(List<RoutineSeriesCreateRequest> series) { this.series = series; }
}
