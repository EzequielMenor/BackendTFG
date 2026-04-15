package com.eze.gymanalytics.api.dto.analytics;

public class DurationStatsDTO {
    private int avgMinutes;
    private int longestMinutes;

    public DurationStatsDTO() {}

    public DurationStatsDTO(int avgMinutes, int longestMinutes) {
        this.avgMinutes = avgMinutes;
        this.longestMinutes = longestMinutes;
    }

    public int getAvgMinutes() {
        return avgMinutes;
    }

    public void setAvgMinutes(int avgMinutes) {
        this.avgMinutes = avgMinutes;
    }

    public int getLongestMinutes() {
        return longestMinutes;
    }

    public void setLongestMinutes(int longestMinutes) {
        this.longestMinutes = longestMinutes;
    }
}
