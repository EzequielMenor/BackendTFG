package com.eze.gymanalytics.api.dto.analytics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class WeeklyVolumeResponse {
    private OffsetDateTime weekStart;
    private BigDecimal totalVolume;

    public WeeklyVolumeResponse(OffsetDateTime weekStart, BigDecimal totalVolume) {
        this.weekStart = weekStart;
        this.totalVolume = totalVolume;
    }

    public OffsetDateTime getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(OffsetDateTime weekStart) {
        this.weekStart = weekStart;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }
}
