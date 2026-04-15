package com.eze.gymanalytics.api.dto.analytics;

public class MuscleDistributionDTO {
    private String muscleGroup;
    private int sets;
    private double percentage;

    public MuscleDistributionDTO() {}

    public MuscleDistributionDTO(String muscleGroup, int sets, double percentage) {
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

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
