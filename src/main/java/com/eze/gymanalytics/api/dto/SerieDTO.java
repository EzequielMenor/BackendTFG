package com.eze.gymanalytics.api.dto;

import java.math.BigDecimal;

public class SerieDTO {
    private Long id;
    private BigDecimal weight;
    private Integer reps;
    private BigDecimal rpe;
    private Boolean isWarmup;
    private Integer setOrder;
    private Boolean isPr;

    public SerieDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public BigDecimal getRpe() { return rpe; }
    public void setRpe(BigDecimal rpe) { this.rpe = rpe; }

    public Boolean getIsWarmup() { return isWarmup; }
    public void setIsWarmup(Boolean isWarmup) { this.isWarmup = isWarmup; }

    public Integer getSetOrder() { return setOrder; }
    public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

    public Boolean getIsPr() { return isPr; }
    public void setIsPr(Boolean isPr) { this.isPr = isPr; }
}
