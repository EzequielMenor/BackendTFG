package com.eze.gymanalytics.api.dto.analytics;

public class VolumeDensityResponse {
    private double currentDensity;
    private double previousDensity;
    private double changePercent;

    public VolumeDensityResponse(double currentDensity, double previousDensity, double changePercent) {
        this.currentDensity = currentDensity;
        this.previousDensity = previousDensity;
        this.changePercent = changePercent;
    }

    public double getCurrentDensity() { return currentDensity; }
    public void setCurrentDensity(double currentDensity) { this.currentDensity = currentDensity; }

    public double getPreviousDensity() { return previousDensity; }
    public void setPreviousDensity(double previousDensity) { this.previousDensity = previousDensity; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }
}
