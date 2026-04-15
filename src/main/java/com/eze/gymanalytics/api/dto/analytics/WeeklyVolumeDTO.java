package com.eze.gymanalytics.api.dto.analytics;

import java.time.OffsetDateTime;

public class WeeklyVolumeDTO {
    private OffsetDateTime weekStart;
    private double totalVolume;

    public WeeklyVolumeDTO() {}

    public WeeklyVolumeDTO(OffsetDateTime weekStart, double totalVolume) {
        this.weekStart = weekStart;
        this.totalVolume = totalVolume;
    }

    public OffsetDateTime getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(OffsetDateTime weekStart) {
        this.weekStart = weekStart;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }
}
