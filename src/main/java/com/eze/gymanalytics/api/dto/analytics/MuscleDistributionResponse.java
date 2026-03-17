package com.eze.gymanalytics.api.dto.analytics;

public class MuscleDistributionResponse {
    private String muscleGroup;
    private Long sets;
    private Double percentage;

    public MuscleDistributionResponse(String muscleGroup, Long sets, Double percentage) {
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.percentage = percentage;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public Long getSets() {
        return sets;
    }

    public void setSets(Long sets) {
        this.sets = sets;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
