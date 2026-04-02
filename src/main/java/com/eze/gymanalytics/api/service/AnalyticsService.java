package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.analytics.*;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.SerieRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final SerieRepository serieRepository;
    private final WorkoutRepository workoutRepository;
    private final ProfileRepository profileRepository;

    public AnalyticsService(SerieRepository serieRepository, WorkoutRepository workoutRepository, ProfileRepository profileRepository) {
        this.serieRepository = serieRepository;
        this.workoutRepository = workoutRepository;
        this.profileRepository = profileRepository;
    }

    public UUID getUserIdByEmail(String email) {
        return profileRepository.findByEmail(email)
                .map(Profile::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public AnalyticsSummaryResponse getSummary(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        long sessions = workoutRepository.countByUserIdAndStartTimeBetween(userId, from, to);
        BigDecimal volume = workoutRepository.sumTotalVolumeByUserIdAndStartTimeBetween(userId, from, to);
        if (volume == null) volume = BigDecimal.ZERO;

        List<OffsetDateTime> allTrainingDays = workoutRepository.findTrainingDaysByUserIdAndRange(userId, OffsetDateTime.now().minusYears(1), OffsetDateTime.now());
        int currentStreak = calculateCurrentStreak(allTrainingDays);

        return new AnalyticsSummaryResponse(sessions, volume, currentStreak);
    }

    public List<RecentPrResponse> getRecentPRs(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> results = serieRepository.findRecentPRsNative(userId, from, to);
        return results.stream().map(row -> new RecentPrResponse(
                row[0] != null ? ((Number) row[0]).longValue() : 0L,
                row[1] != null ? (String) row[1] : "Ejercicio",
                convertToOffsetDateTime(row[2]),
                row[3] != null ? BigDecimal.valueOf(((Number) row[3]).doubleValue()) : BigDecimal.ZERO
        )).collect(Collectors.toList());
    }

    public List<TopExerciseResponse> getTopExercises(UUID userId, int limit) {
        List<Object[]> results = serieRepository.findTopExercisesNative(userId, limit);
        List<TopExerciseResponse> topExercises = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            topExercises.add(new TopExerciseResponse(
                    i + 1,
                    row[1] != null ? (String) row[1] : "Ejercicio",
                    row[2] != null ? BigDecimal.valueOf(((Number) row[2]).doubleValue()) : BigDecimal.ZERO
            ));
        }
        return topExercises;
    }

    public List<WeeklyVolumeResponse> getWeeklyVolume(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> results = serieRepository.findWeeklyVolumeNative(userId, from, to);
        return results.stream().map(row -> new WeeklyVolumeResponse(
                convertToOffsetDateTime(row[0]),
                row[1] != null ? BigDecimal.valueOf(((Number) row[1]).doubleValue()) : BigDecimal.ZERO
        )).collect(Collectors.toList());
    }

    public List<MuscleDistributionResponse> getMuscleDistribution(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> results = serieRepository.findMuscleDistributionNative(userId, from, to);
        List<MuscleDistributionResponse> distribution = results.stream().map(row -> new MuscleDistributionResponse(
                row[0] != null ? (String) row[0] : "Otros",
                row[1] != null ? ((Number) row[1]).longValue() : 0L,
                0.0
        )).collect(Collectors.toList());

        long totalSets = distribution.stream().mapToLong(MuscleDistributionResponse::getSets).sum();
        if (totalSets > 0) {
            distribution.forEach(d -> d.setPercentage((double) d.getSets() * 100.0 / totalSets));
        }
        distribution.sort(Comparator.comparing(MuscleDistributionResponse::getSets).reversed());
        return distribution;
    }

    public TrainingDaysResponse getTrainingDays(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<OffsetDateTime> trainingDays = workoutRepository.findTrainingDaysByUserIdAndRange(userId, from, to);
        List<OffsetDateTime> historicalDays = workoutRepository.findTrainingDaysByUserIdAndRange(userId, OffsetDateTime.now().minusYears(2), OffsetDateTime.now());
        
        int currentStreak = calculateCurrentStreak(historicalDays);
        int bestStreak = calculateBestStreak(historicalDays);

        long periodDays = ChronoUnit.DAYS.between(from, to);
        double weeks = periodDays / 7.0;
        long uniqueTrainingDays = trainingDays.stream()
                .map(OffsetDateTime::toLocalDate)
                .distinct()
                .count();
        double avgDaysPerWeek = weeks > 0 ? Math.round((uniqueTrainingDays / weeks) * 10.0) / 10.0 : 0.0;

        return new TrainingDaysResponse(trainingDays, currentStreak, bestStreak, avgDaysPerWeek);
    }

    public DurationStatsResponse getDurationStats(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> results = workoutRepository.findDurationStatsNative(userId, from, to);
        if (results == null || results.isEmpty() || results.get(0)[0] == null) {
            return new DurationStatsResponse(0, 0);
        }
        Object[] row = results.get(0);
        return new DurationStatsResponse(
                row[0] != null ? ((Number) row[0]).intValue() : 0,
                row[1] != null ? ((Number) row[1]).intValue() : 0
        );
    }

    private OffsetDateTime convertToOffsetDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof OffsetDateTime) return (OffsetDateTime) obj;
        if (obj instanceof java.time.Instant) return ((java.time.Instant) obj).atOffset(java.time.ZoneOffset.UTC);
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toInstant().atOffset(java.time.ZoneOffset.UTC);
        if (obj instanceof java.time.LocalDateTime) return ((java.time.LocalDateTime) obj).atOffset(java.time.ZoneOffset.UTC);
        return OffsetDateTime.now();
    }

    private int calculateCurrentStreak(List<OffsetDateTime> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        
        Set<LocalDate> uniqueDates = dates.stream()
                .map(OffsetDateTime::toLocalDate)
                .collect(Collectors.toCollection(TreeSet::new));
        
        List<LocalDate> sortedDates = new ArrayList<>(uniqueDates);
        Collections.reverse(sortedDates);
        
        LocalDate today = LocalDate.now();
        LocalDate lastTraining = sortedDates.get(0);
        
        if (ChronoUnit.DAYS.between(lastTraining, today) > 1) {
            return 0;
        }
        
        int streak = 1;
        for (int i = 0; i < sortedDates.size() - 1; i++) {
            if (ChronoUnit.DAYS.between(sortedDates.get(i + 1), sortedDates.get(i)) == 1) {
                streak++;
            } else if (ChronoUnit.DAYS.between(sortedDates.get(i + 1), sortedDates.get(i)) == 0) {
                continue;
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateBestStreak(List<OffsetDateTime> dates) {
        if (dates == null || dates.isEmpty()) return 0;

        List<LocalDate> sortedDates = dates.stream()
                .map(OffsetDateTime::toLocalDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;

        for (LocalDate date : sortedDates) {
            if (lastDate == null || ChronoUnit.DAYS.between(lastDate, date) == 1) {
                currentStreak++;
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = 1;
            }
            lastDate = date;
        }
        return Math.max(maxStreak, currentStreak);
    }

    public List<Progression1RMDTO> get1RMProgression(UUID userId, Long exerciseId) {
        return serieRepository.find1RMProgression(userId, exerciseId);
    }

    public List<EffectiveVolumeDTO> getEffectiveVolume(UUID userId, OffsetDateTime startDate) {
        return serieRepository.findEffectiveVolume(userId, startDate);
    }

    // ── EZE-168: Volume Density ────────────────────────────────────────────────

    public VolumeDensityResponse getVolumeDensity(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime minus30 = now.minusDays(30);
        OffsetDateTime minus60 = minus30.minusDays(30);

        Double current  = serieRepository.findVolumeDensity(userId, minus30, now);
        Double previous = serieRepository.findVolumeDensity(userId, minus60, minus30);

        double cur  = current  != null ? current  : 0.0;
        double prev = previous != null ? previous : 0.0;
        double change = prev > 0 ? Math.round(((cur - prev) / prev) * 1000.0) / 10.0 : 0.0;

        return new VolumeDensityResponse(
                Math.round(cur  * 100.0) / 100.0,
                Math.round(prev * 100.0) / 100.0,
                change);
    }

    // ── EZE-168: Training Style ────────────────────────────────────────────────

    public TrainingStyleResponse getTrainingStyle(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> results = serieRepository.findTrainingStyleCounts(userId, from, to);
        Object[] row = (results != null && !results.isEmpty()) ? results.get(0) : null;
        int strength    = row != null && row[0] != null ? ((Number) row[0]).intValue() : 0;
        int hypertrophy = row != null && row[1] != null ? ((Number) row[1]).intValue() : 0;
        int endurance   = row != null && row[2] != null ? ((Number) row[2]).intValue() : 0;
        return new TrainingStyleResponse(strength, hypertrophy, endurance);
    }

    // ── EZE-168: Weekly Rhythm ─────────────────────────────────────────────────

    public WeeklyRhythmResponse getWeeklyRhythm(UUID userId) {
        OffsetDateTime sixMonthsAgo = OffsetDateTime.now().minusMonths(6);
        List<Object[]> results = workoutRepository.findWeeklyRhythm(userId, sixMonthsAgo);

        // Build a 7-element array (index 0=Mon … 6=Sun), default 0
        int[] counts = new int[7];
        for (Object[] row : results) {
            int dayIndex   = ((Number) row[0]).intValue();
            int sessionCnt = ((Number) row[1]).intValue();
            if (dayIndex >= 0 && dayIndex < 7) {
                counts[dayIndex] = sessionCnt;
            }
        }

        List<Integer> list = new java.util.ArrayList<>();
        for (int c : counts) list.add(c);
        return new WeeklyRhythmResponse(list);
    }

    // ── EZE-169: Exercise Trend Engine ────────────────────────────────────────

    public ExerciseTrendResponse getExerciseTrend(UUID userId, Long exerciseId) {
        // Last 3 sessions
        List<Object[]> recent = serieRepository.findRecentExerciseSessions(userId, exerciseId, 3);
        if (recent.size() < 3) {
            String seed = exerciseId + "|NEW|" + java.time.YearMonth.now();
            return new ExerciseTrendResponse("NEW", seed);
        }

        double recentAvg = recent.stream()
                .mapToDouble(r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0)
                .average().orElse(0.0);

        // Sessions from ~1 month ago (same count)
        OffsetDateTime monthEnd   = OffsetDateTime.now().minusDays(25);
        OffsetDateTime monthStart = monthEnd.minusDays(40);
        List<Object[]> older = serieRepository.findExerciseSessionsInRange(userId, exerciseId, monthStart, monthEnd, 3);

        if (older.isEmpty() || recentAvg == 0.0) {
            String seed = exerciseId + "|NEW|" + java.time.YearMonth.now();
            return new ExerciseTrendResponse("NEW", seed);
        }

        double olderAvg = older.stream()
                .mapToDouble(r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0)
                .average().orElse(0.0);

        double changePercent = olderAvg > 0 ? ((recentAvg - olderAvg) / olderAvg) * 100.0 : 0.0;

        String status;
        if      (changePercent >  3.0)  status = "OVERLOAD";
        else if (changePercent < -3.0)  status = "REGRESSION";
        else                            status = "STAGNANT";

        String seed = exerciseId + "|" + status + "|" + java.time.YearMonth.now();
        return new ExerciseTrendResponse(status, seed);
    }
}
