package com.eze.gymanalytics.api.dto.analytics;

import java.math.BigDecimal;

public class AnalyticsSummaryResponse {
    private long sessionCount;
    private BigDecimal totalVolume;
    private int currentStreak;

    public AnalyticsSummaryResponse(long sessionCount, BigDecimal totalVolume, int currentStreak) {
        this.sessionCount = sessionCount;
        this.totalVolume = totalVolume;
        this.currentStreak = currentStreak;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
}
