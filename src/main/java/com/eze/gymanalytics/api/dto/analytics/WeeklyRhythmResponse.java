package com.eze.gymanalytics.api.dto.analytics;

import java.util.List;

public class WeeklyRhythmResponse {
    /** Index 0 = Monday … 6 = Sunday */
    private List<Integer> sessionsByDayOfWeek;

    public WeeklyRhythmResponse(List<Integer> sessionsByDayOfWeek) {
        this.sessionsByDayOfWeek = sessionsByDayOfWeek;
    }

    public List<Integer> getSessionsByDayOfWeek() { return sessionsByDayOfWeek; }
    public void setSessionsByDayOfWeek(List<Integer> sessionsByDayOfWeek) { this.sessionsByDayOfWeek = sessionsByDayOfWeek; }
}
