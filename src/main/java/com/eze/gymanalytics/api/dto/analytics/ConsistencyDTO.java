package com.eze.gymanalytics.api.dto.analytics;

import java.time.OffsetDateTime;
import java.util.List;

public class ConsistencyDTO {
    private int currentStreak;
    private int bestStreak;
    private double avgDaysPerWeek;
    private List<OffsetDateTime> trainingDays;

    public ConsistencyDTO() {}

    public ConsistencyDTO(int currentStreak, int bestStreak, double avgDaysPerWeek, List<OffsetDateTime> trainingDays) {
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.avgDaysPerWeek = avgDaysPerWeek;
        this.trainingDays = trainingDays;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public double getAvgDaysPerWeek() {
        return avgDaysPerWeek;
    }

    public void setAvgDaysPerWeek(double avgDaysPerWeek) {
        this.avgDaysPerWeek = avgDaysPerWeek;
    }

    public List<OffsetDateTime> getTrainingDays() {
        return trainingDays;
    }

    public void setTrainingDays(List<OffsetDateTime> trainingDays) {
        this.trainingDays = trainingDays;
    }
}
