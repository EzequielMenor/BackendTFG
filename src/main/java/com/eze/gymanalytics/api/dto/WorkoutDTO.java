package com.eze.gymanalytics.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class WorkoutDTO {
    private Long id;
    private String name;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String notes;
    private BigDecimal totalVolume;
    private List<WorkoutExerciseDTO> exercises;

    public WorkoutDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

    public List<WorkoutExerciseDTO> getExercises() { return exercises; }
    public void setExercises(List<WorkoutExerciseDTO> exercises) { this.exercises = exercises; }
}
