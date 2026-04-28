package com.eze.gymanalytics.api.dto.routines;

import java.math.BigDecimal;

public class RoutineSeriesDTO {
  private Long id;
  private Integer setOrder;
  private BigDecimal targetWeight;
  private Integer targetRepsMin;
  private Integer targetRepsMax;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Integer getSetOrder() { return setOrder; }
  public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

  public BigDecimal getTargetWeight() { return targetWeight; }
  public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

  public Integer getTargetRepsMin() { return targetRepsMin; }
  public void setTargetRepsMin(Integer targetRepsMin) { this.targetRepsMin = targetRepsMin; }

  public Integer getTargetRepsMax() { return targetRepsMax; }
  public void setTargetRepsMax(Integer targetRepsMax) { this.targetRepsMax = targetRepsMax; }
}
