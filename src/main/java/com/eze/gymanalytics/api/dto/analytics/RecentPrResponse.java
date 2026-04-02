package com.eze.gymanalytics.api.dto.analytics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class RecentPrResponse {
    private Long exerciseId;
    private String exerciseName;
    private OffsetDateTime date;
    private BigDecimal estimated1Rm;  // kept for backwards compatibility — now holds maxWeight

    public RecentPrResponse(Long exerciseId, String exerciseName, OffsetDateTime date, BigDecimal maxWeight) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.date = date;
        this.estimated1Rm = maxWeight;
    }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public OffsetDateTime getDate() { return date; }
    public void setDate(OffsetDateTime date) { this.date = date; }

    public BigDecimal getEstimated1Rm() { return estimated1Rm; }
    public void setEstimated1Rm(BigDecimal estimated1Rm) { this.estimated1Rm = estimated1Rm; }
}
