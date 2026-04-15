package com.eze.gymanalytics.api.dto.analytics;

import java.time.OffsetDateTime;

public class RecentPrDTO {
    private String exerciseName;
    private double estimated1Rm;
    private OffsetDateTime date;

    public RecentPrDTO() {}

    public RecentPrDTO(String exerciseName, double estimated1Rm, OffsetDateTime date) {
        this.exerciseName = exerciseName;
        this.estimated1Rm = estimated1Rm;
        this.date = date;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public double getEstimated1Rm() {
        return estimated1Rm;
    }

    public void setEstimated1Rm(double estimated1Rm) {
        this.estimated1Rm = estimated1Rm;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }
}
