package com.eze.gymanalytics.api.dto.analytics;

public class DurationStatsResponse {
    private Integer avgMinutes;
    private Integer longestMinutes;

    public DurationStatsResponse(Integer avgMinutes, Integer longestMinutes) {
        this.avgMinutes = avgMinutes;
        this.longestMinutes = longestMinutes;
    }

    public Integer getAvgMinutes() {
        return avgMinutes;
    }

    public void setAvgMinutes(Integer avgMinutes) {
        this.avgMinutes = avgMinutes;
    }

    public Integer getLongestMinutes() {
        return longestMinutes;
    }

    public void setLongestMinutes(Integer longestMinutes) {
        this.longestMinutes = longestMinutes;
    }
}
