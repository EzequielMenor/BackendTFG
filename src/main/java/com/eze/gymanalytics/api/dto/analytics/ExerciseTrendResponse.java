package com.eze.gymanalytics.api.dto.analytics;

public class ExerciseTrendResponse {
    /** NEW | OVERLOAD | STAGNANT | REGRESSION */
    private String status;
    /** Deterministic seed: "{exerciseId}|{status}|{YYYY-MM}" */
    private String seed;

    public ExerciseTrendResponse(String status, String seed) {
        this.status = status;
        this.seed = seed;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }
}
