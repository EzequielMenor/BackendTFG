package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.analytics.AnalyticsSummaryDTO;
import com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO;
import com.eze.gymanalytics.api.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Endpoint to get the 1RM progression for a specific exercise and user.
     * 
     * @param userId The ID of the user (in future will be extracted from JWT)
     * @param exerciseId The ID of the exercise
     * @return List of date and estimated 1RM pairs
     */
    @GetMapping("/1rm-progression")
    public ResponseEntity<List<Progression1RMDTO>> get1RMProgression(
            @RequestParam UUID userId,
            @RequestParam Long exerciseId) {
        
        List<Progression1RMDTO> progression = analyticsService.get1RMProgression(userId, exerciseId);
        return ResponseEntity.ok(progression);
    }

    /**
     * Endpoint to get the effective volume (sets where RPE >= 7 & not warmup)
     * by muscle group, for a specific user and since a specific date.
     * 
     * @param userId The ID of the user
     * @param days Number of days back to look for effective volume (default 30)
     * @return List of muscle groups and effective set counts
     */
    @GetMapping("/effective-volume")
    public ResponseEntity<List<EffectiveVolumeDTO>> getEffectiveVolume(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "30") int days) {
        
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        List<EffectiveVolumeDTO> volume = analyticsService.getEffectiveVolume(userId, startDate);
        return ResponseEntity.ok(volume);
    }

    /**
     * Endpoint to get an aggregated training summary for a user within a date window.
     *
     * @param userId The ID of the user
     * @param from   Start of the date window (ISO 8601, e.g. 2025-01-01T00:00:00Z)
     * @param to     End of the date window (ISO 8601, e.g. 2025-12-31T23:59:59Z)
     * @return AnalyticsSummaryDTO with totalWorkouts, totalVolume, topMuscleGroup, avgDurationMinutes
     */
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDTO> getSummary(
            @RequestParam UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        System.out.println("DEBUG: Received GET /summary with params userId=" + userId + ", from=" + from + ", to=" + to);
        AnalyticsSummaryDTO summary = analyticsService.getSummary(userId, from, to);
        return ResponseEntity.ok(summary);
    }
}
