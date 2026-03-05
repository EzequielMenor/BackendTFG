package com.eze.gymanalytics.api.dto.analytics;

public class EffectiveVolumeDTO {
    private String muscleGroup;
    private Long effectiveSets;

    public EffectiveVolumeDTO(String muscleGroup, Long effectiveSets) {
        this.muscleGroup = muscleGroup;
        this.effectiveSets = effectiveSets;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public Long getEffectiveSets() {
        return effectiveSets;
    }

    public void setEffectiveSets(Long effectiveSets) {
        this.effectiveSets = effectiveSets;
    }
}
