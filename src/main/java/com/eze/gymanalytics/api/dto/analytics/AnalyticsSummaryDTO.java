package com.eze.gymanalytics.api.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class AnalyticsSummaryDTO {

    private long totalWorkouts;
    private BigDecimal totalVolume;
    private String topMuscleGroup;
    private double avgDurationMinutes;

    public AnalyticsSummaryDTO(long totalWorkouts,
                               BigDecimal totalVolume,
                               String topMuscleGroup,
                               double avgDurationMinutes) {
        this.totalWorkouts = totalWorkouts;
        this.totalVolume = totalVolume;
        this.topMuscleGroup = topMuscleGroup;
        this.avgDurationMinutes = avgDurationMinutes;
    }

    public long getTotalWorkouts() {
        return totalWorkouts;
    }

    @JsonProperty("sessionCount")
    public long getSessionCount() {
        return totalWorkouts;
    }

    public void setTotalWorkouts(long totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getTopMuscleGroup() {
        return topMuscleGroup;
    }

    public void setTopMuscleGroup(String topMuscleGroup) {
        this.topMuscleGroup = topMuscleGroup;
    }

    public double getAvgDurationMinutes() {
        return avgDurationMinutes;
    }

    public void setAvgDurationMinutes(double avgDurationMinutes) {
        this.avgDurationMinutes = avgDurationMinutes;
    }
}
