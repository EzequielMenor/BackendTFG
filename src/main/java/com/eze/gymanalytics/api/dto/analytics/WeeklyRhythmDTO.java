package com.eze.gymanalytics.api.dto.analytics;

import java.util.List;

public class WeeklyRhythmDTO {
    private List<Integer> sessionsByDayOfWeek;

    public WeeklyRhythmDTO() {}

    public WeeklyRhythmDTO(List<Integer> sessionsByDayOfWeek) {
        this.sessionsByDayOfWeek = sessionsByDayOfWeek;
    }

    public List<Integer> getSessionsByDayOfWeek() {
        return sessionsByDayOfWeek;
    }

    public void setSessionsByDayOfWeek(List<Integer> sessionsByDayOfWeek) {
        this.sessionsByDayOfWeek = sessionsByDayOfWeek;
    }
}
