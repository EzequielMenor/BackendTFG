package com.eze.gymanalytics.api.dto;

public class AdminStatsDTO {

    private long totalUsers;
    private long totalWorkouts;
    private long totalExercises;
    private long activeLastWeek;

    public AdminStatsDTO() {}

    public AdminStatsDTO(long totalUsers, long totalWorkouts, long totalExercises, long activeLastWeek) {
        this.totalUsers = totalUsers;
        this.totalWorkouts = totalWorkouts;
        this.totalExercises = totalExercises;
        this.activeLastWeek = activeLastWeek;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(long totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public long getTotalExercises() { return totalExercises; }
    public void setTotalExercises(long totalExercises) { this.totalExercises = totalExercises; }

    public long getActiveLastWeek() { return activeLastWeek; }
    public void setActiveLastWeek(long activeLastWeek) { this.activeLastWeek = activeLastWeek; }
}
