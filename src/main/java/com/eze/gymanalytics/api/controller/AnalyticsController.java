package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.analytics.AnalyticsSummaryDTO;
import com.eze.gymanalytics.api.dto.analytics.ConsistencyDTO;
import com.eze.gymanalytics.api.dto.analytics.DurationStatsDTO;
import com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.MuscleDistributionDTO;
import com.eze.gymanalytics.api.dto.analytics.RecentPrDTO;
import com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO;
import com.eze.gymanalytics.api.dto.analytics.TopExerciseDTO;
import com.eze.gymanalytics.api.dto.analytics.TrainingStyleDTO;
import com.eze.gymanalytics.api.dto.analytics.VolumeDensityDTO;
import com.eze.gymanalytics.api.dto.analytics.WeeklyRhythmDTO;
import com.eze.gymanalytics.api.dto.analytics.WeeklyVolumeDTO;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ProfileRepository profileRepository;

    public AnalyticsController(AnalyticsService analyticsService, ProfileRepository profileRepository) {
        this.analyticsService = analyticsService;
        this.profileRepository = profileRepository;
    }

    private java.util.UUID requireUserId(@AuthenticationPrincipal String email) {
        return profileRepository.findByEmail(email)
                .map(com.eze.gymanalytics.api.model.Profile::getId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado para email: " + email));
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
            @AuthenticationPrincipal String email,
            @RequestParam Long exerciseId) {
        
        UUID userId = requireUserId(email);
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
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "30") int days) {
        
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        UUID userId = requireUserId(email);
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
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        UUID userId = requireUserId(email);
        System.out.println("DEBUG: Received GET /summary with userId=" + userId + ", from=" + from + ", to=" + to);
        AnalyticsSummaryDTO summary = analyticsService.getSummary(userId, from, to);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/recent-prs")
    public ResponseEntity<List<RecentPrDTO>> getRecentPRs(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getRecentPRs(userId, from, to));
    }

    @GetMapping("/top-exercises")
    public ResponseEntity<List<TopExerciseDTO>> getTopExercises(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "5") int limit) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getTopExercises(userId, limit));
    }

    @GetMapping("/weekly-volume")
    public ResponseEntity<List<WeeklyVolumeDTO>> getWeeklyVolume(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getWeeklyVolume(userId, from, to));
    }

    @GetMapping("/muscle-distribution")
    public ResponseEntity<List<MuscleDistributionDTO>> getMuscleDistribution(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getMuscleDistribution(userId, from, to));
    }

    @GetMapping("/training-days")
    public ResponseEntity<ConsistencyDTO> getTrainingDays(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getTrainingDays(userId, from, to));
    }

    @GetMapping("/duration-stats")
    public ResponseEntity<DurationStatsDTO> getDurationStats(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getDurationStats(userId, from, to));
    }

    @GetMapping("/volume-density")
    public ResponseEntity<VolumeDensityDTO> getVolumeDensity(@AuthenticationPrincipal String email) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getVolumeDensity(userId));
    }

    @GetMapping("/training-style")
    public ResponseEntity<TrainingStyleDTO> getTrainingStyle(
            @AuthenticationPrincipal String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getTrainingStyle(userId, from, to));
    }

    @GetMapping("/weekly-rhythm")
    public ResponseEntity<WeeklyRhythmDTO> getWeeklyRhythm(@AuthenticationPrincipal String email) {
        UUID userId = requireUserId(email);
        return ResponseEntity.ok(analyticsService.getWeeklyRhythm(userId));
    }
}
