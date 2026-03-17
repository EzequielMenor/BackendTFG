package com.eze.gymanalytics.api.dto.analytics;

import java.math.BigDecimal;

public class TopExerciseResponse {
    private int rank;
    private String exerciseName;
    private BigDecimal best1Rm;

    public TopExerciseResponse(int rank, String exerciseName, BigDecimal best1Rm) {
        this.rank = rank;
        this.exerciseName = exerciseName;
        this.best1Rm = best1Rm;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public BigDecimal getBest1Rm() {
        return best1Rm;
    }

    public void setBest1Rm(BigDecimal best1Rm) {
        this.best1Rm = best1Rm;
    }
}
