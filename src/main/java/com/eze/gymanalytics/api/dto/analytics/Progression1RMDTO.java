package com.eze.gymanalytics.api.dto.analytics;

import java.time.OffsetDateTime;

public class Progression1RMDTO {
    private OffsetDateTime date;
    private Double estimated1Rm;

    public Progression1RMDTO(OffsetDateTime date, Double estimated1Rm) {
        this.date = date;
        this.estimated1Rm = estimated1Rm;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public Double getEstimated1Rm() {
        return estimated1Rm;
    }

    public void setEstimated1Rm(Double estimated1Rm) {
        this.estimated1Rm = estimated1Rm;
    }
}
