package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.analytics.*;
import com.eze.gymanalytics.api.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getSummary(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getSummary: ", e);
            throw e;
        }
    }

    @GetMapping("/recent-prs")
    public ResponseEntity<List<RecentPrResponse>> getRecentPRs(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getRecentPRs(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getRecentPRs: ", e);
            throw e;
        }
    }

    @GetMapping("/top-exercises")
    public ResponseEntity<List<TopExerciseResponse>> getTopExercises(
            Principal principal,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getTopExercises(userId, limit));
        } catch (Exception e) {
            log.error("Error en getTopExercises: ", e);
            throw e;
        }
    }

    @GetMapping("/weekly-volume")
    public ResponseEntity<List<WeeklyVolumeResponse>> getWeeklyVolume(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getWeeklyVolume(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getWeeklyVolume: ", e);
            throw e;
        }
    }

    @GetMapping("/muscle-distribution")
    public ResponseEntity<List<MuscleDistributionResponse>> getMuscleDistribution(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getMuscleDistribution(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getMuscleDistribution: ", e);
            throw e;
        }
    }

    @GetMapping("/training-days")
    public ResponseEntity<TrainingDaysResponse> getTrainingDays(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getTrainingDays(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getTrainingDays: ", e);
            throw e;
        }
    }

    @GetMapping("/duration-stats")
    public ResponseEntity<DurationStatsResponse> getDurationStats(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getDurationStats(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getDurationStats: ", e);
            throw e;
        }
    }

    @GetMapping("/1rm-progression")
    public ResponseEntity<List<Progression1RMDTO>> get1RMProgression(
            Principal principal,
            @RequestParam Long exerciseId) {
        UUID userId = analyticsService.getUserIdByEmail(principal.getName());
        return ResponseEntity.ok(analyticsService.get1RMProgression(userId, exerciseId));
    }

    @GetMapping("/effective-volume")
    public ResponseEntity<List<EffectiveVolumeDTO>> getEffectiveVolume(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate) {
        UUID userId = analyticsService.getUserIdByEmail(principal.getName());
        return ResponseEntity.ok(analyticsService.getEffectiveVolume(userId, startDate));
    }

    // ── EZE-168 ────────────────────────────────────────────────────────────────

    @GetMapping("/volume-density")
    public ResponseEntity<VolumeDensityResponse> getVolumeDensity(Principal principal) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getVolumeDensity(userId));
        } catch (Exception e) {
            log.error("Error en getVolumeDensity: ", e);
            throw e;
        }
    }

    @GetMapping("/training-style")
    public ResponseEntity<TrainingStyleResponse> getTrainingStyle(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getTrainingStyle(userId, from, to));
        } catch (Exception e) {
            log.error("Error en getTrainingStyle: ", e);
            throw e;
        }
    }

    @GetMapping("/weekly-rhythm")
    public ResponseEntity<WeeklyRhythmResponse> getWeeklyRhythm(Principal principal) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getWeeklyRhythm(userId));
        } catch (Exception e) {
            log.error("Error en getWeeklyRhythm: ", e);
            throw e;
        }
    }

    // ── EZE-169 ────────────────────────────────────────────────────────────────

    @GetMapping("/exercise/{id}/trend")
    public ResponseEntity<ExerciseTrendResponse> getExerciseTrend(
            Principal principal,
            @PathVariable Long id) {
        try {
            UUID userId = analyticsService.getUserIdByEmail(principal.getName());
            return ResponseEntity.ok(analyticsService.getExerciseTrend(userId, id));
        } catch (Exception e) {
            log.error("Error en getExerciseTrend: ", e);
            throw e;
        }
    }
}
