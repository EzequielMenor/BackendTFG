package com.eze.gymanalytics.api.dto;

import java.math.BigDecimal;

/**
 * DTO para la lista de entrenamientos en el panel de administración.
 * Incluye datos básicos del workout + email del usuario propietario.
 */
public class AdminWorkoutDTO {

    private Long id;
    private String name;
    private String userEmail;
    private String startTime;
    private String endTime;
    private String notes;
    private BigDecimal totalVolume;
    private int exerciseCount;

    public AdminWorkoutDTO() {}

    public AdminWorkoutDTO(Long id, String name, String userEmail,
                           String startTime, String endTime,
                           String notes, BigDecimal totalVolume, int exerciseCount) {
        this.id = id;
        this.name = name;
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
        this.totalVolume = totalVolume;
        this.exerciseCount = exerciseCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

    public int getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }
}
