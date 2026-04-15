package com.eze.gymanalytics.api.dto.analytics;

public class TrainingStyleDTO {
    private int strengthSets;
    private int hypertrophySets;
    private int enduranceSets;

    public TrainingStyleDTO() {}

    public TrainingStyleDTO(int strengthSets, int hypertrophySets, int enduranceSets) {
        this.strengthSets = strengthSets;
        this.hypertrophySets = hypertrophySets;
        this.enduranceSets = enduranceSets;
    }

    public int getStrengthSets() {
        return strengthSets;
    }

    public void setStrengthSets(int strengthSets) {
        this.strengthSets = strengthSets;
    }

    public int getHypertrophySets() {
        return hypertrophySets;
    }

    public void setHypertrophySets(int hypertrophySets) {
        this.hypertrophySets = hypertrophySets;
    }

    public int getEnduranceSets() {
        return enduranceSets;
    }

    public void setEnduranceSets(int enduranceSets) {
        this.enduranceSets = enduranceSets;
    }
}
