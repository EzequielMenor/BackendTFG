package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.analytics.AnalyticsSummaryDTO;
import com.eze.gymanalytics.api.dto.analytics.ConsistencyDTO;
import com.eze.gymanalytics.api.dto.analytics.DurationStatsDTO;
import com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.MuscleDistributionDTO;
import com.eze.gymanalytics.api.dto.analytics.RecentPrDTO;
import com.eze.gymanalytics.api.dto.analytics.TopExerciseDTO;
import com.eze.gymanalytics.api.dto.analytics.TrainingStyleDTO;
import com.eze.gymanalytics.api.dto.analytics.VolumeDensityDTO;
import com.eze.gymanalytics.api.dto.analytics.WeeklyRhythmDTO;
import com.eze.gymanalytics.api.dto.analytics.WeeklyVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO;
import com.eze.gymanalytics.api.repository.SerieRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import com.eze.gymanalytics.api.repository.WorkoutExerciseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class AnalyticsService {

    private final SerieRepository serieRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsService(SerieRepository serieRepository, 
                            WorkoutRepository workoutRepository,
                            WorkoutExerciseRepository workoutExerciseRepository) {
        this.serieRepository = serieRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
    }

    /**
     * Calculates the estimated 1RM progression for a given user and exercise,
     * using the Brzycki formula and filtering out warmup sets and sets with reps > 12.
     *
     * @param userId The ID of the user
     * @param exerciseId The ID of the exercise
     * @return List of progression points mapping Date to Estimated 1RM
     */
    public List<Progression1RMDTO> get1RMProgression(UUID userId, Long exerciseId) {
        return serieRepository.find1RMProgression(userId, exerciseId);
    }

    /**
     * Calculates the effective volume (number of effective sets) per muscle group
     * since a given start date. Filters out warmup sets and sets with RPE < 7.
     *
     * @param userId The ID of the user
     * @param startDate The date to start counting from (e.g., 30 days ago)
     * @return List mapping Muscle Group to Number of Effective Sets
     */
    public List<EffectiveVolumeDTO> getEffectiveVolume(UUID userId, OffsetDateTime startDate) {
        return serieRepository.findEffectiveVolume(userId, startDate);
    }

/**
     * Returns an aggregated summary of the user's training within a date window.
     * Includes total workouts, total volume, top muscle group, and average duration.
     *
     * @param userId The ID of the user
     * @param from   Start of the date window (inclusive)
     * @param to     End of the date window (inclusive)
     * @return AnalyticsSummaryDTO with aggregated training data
     */
    public AnalyticsSummaryDTO getSummary(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        // 1. Count total workouts in period
        long totalWorkouts = workoutRepository.countByUserIdAndStartTimeBetween(userId, from, to);
        
        // 2. Calculate total volume (sum of weight * reps for effective sets)
        BigDecimal totalVolume = serieRepository.sumVolumeByUserAndDateRange(userId, from, to);
        if (totalVolume == null) {
            totalVolume = BigDecimal.ZERO;
        }
        
        // 3. Find top muscle group
        String topMuscleGroup = workoutExerciseRepository.findTopMuscleGroupByUserAndDateRange(userId, from, to);
        if (topMuscleGroup == null) {
            topMuscleGroup = null; // N/A
        }
        
        // 4. Calculate average duration
        double avgDurationMinutes = workoutRepository.avgDurationByUserAndDateRange(userId, from, to);
        
        return new AnalyticsSummaryDTO(totalWorkouts, totalVolume, topMuscleGroup, avgDurationMinutes);
    }

    public List<RecentPrDTO> getRecentPRs(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        // PRs recientes: mejor 1RM estimado por ejercicio dentro del período.
        String sql = """
            SELECT DISTINCT ON (e.id)
              e.name AS exerciseName,
              (s.weight * (36.0 / (37.0 - s.reps))) AS estimated1Rm,
              w.start_time AS date
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN workouts w ON we.workout_id = w.id
            JOIN exercises e ON we.exercise_id = e.id
            WHERE w.user_id = :userId
              AND s.is_warmup = false
              AND s.reps <= 12
              AND w.start_time >= :from
              AND w.start_time <= :to
            ORDER BY e.id, estimated1Rm DESC, w.start_time DESC
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(sql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<RecentPrDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            String exerciseName = (String) r[0];
            double estimated1Rm = toDouble(r[1]);
            OffsetDateTime date = toOffsetDateTime(r[2]);
            out.add(new RecentPrDTO(exerciseName, estimated1Rm, date));
        }

        return out.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(5)
                .collect(toList());
    }

    public List<TopExerciseDTO> getTopExercises(UUID userId, int limit) {
        String sql = """
            SELECT e.name AS exerciseName,
                   MAX(s.weight * (36.0 / (37.0 - s.reps))) AS best1Rm
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN workouts w ON we.workout_id = w.id
            JOIN exercises e ON we.exercise_id = e.id
            WHERE w.user_id = :userId
              AND s.is_warmup = false
              AND s.reps <= 12
            GROUP BY e.name
            ORDER BY best1Rm DESC
            LIMIT :limit
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(sql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("limit", limit)
                .getResultList();

        List<TopExerciseDTO> out = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            out.add(new TopExerciseDTO(rank++, (String) r[0], toDouble(r[1])));
        }
        return out;
    }

    public List<WeeklyVolumeDTO> getWeeklyVolume(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        String sql = """
            SELECT date_trunc('week', w.start_time) AS weekStart,
                   COALESCE(SUM(s.weight * s.reps), 0) AS totalVolume
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId
              AND s.is_warmup = false
              AND w.start_time >= :from
              AND w.start_time <= :to
            GROUP BY weekStart
            ORDER BY weekStart ASC
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(sql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<WeeklyVolumeDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new WeeklyVolumeDTO(toOffsetDateTime(r[0]), toDouble(r[1])));
        }
        return out;
    }

    public List<MuscleDistributionDTO> getMuscleDistribution(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        String sql = """
            WITH per AS (
                SELECT e.muscle_group AS muscleGroup,
                       COUNT(s.id) AS sets
                FROM series s
                JOIN workout_exercises we ON s.workout_exercise_id = we.id
                JOIN workouts w ON we.workout_id = w.id
                JOIN exercises e ON we.exercise_id = e.id
                WHERE w.user_id = :userId
                  AND s.is_warmup = false
                  AND (s.rpe IS NULL OR s.rpe >= 7.0)
                  AND w.start_time >= :from
                  AND w.start_time <= :to
                GROUP BY e.muscle_group
            ), totals AS (
                SELECT COALESCE(SUM(sets), 0) AS totalSets FROM per
            )
            SELECT per.muscleGroup,
                   per.sets,
                   (per.sets * 100.0 / NULLIF(totals.totalSets, 0)) AS percentage
            FROM per
            CROSS JOIN totals
            ORDER BY per.sets DESC
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(sql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<MuscleDistributionDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            out.add(new MuscleDistributionDTO(
                    (String) r[0],
                    ((Number) r[1]).intValue(),
                    toDouble(r[2])
            ));
        }
        return out;
    }

    public ConsistencyDTO getTrainingDays(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        // Lista para el período (cliente la usa para heatmap + cálculo de media en cliente en la spec,
        // pero tu modelo espera avgDaysPerWeek desde backend).
        String daysSql = """
            SELECT DISTINCT DATE(w.start_time) AS day
            FROM workouts w
            WHERE w.user_id = :userId
              AND w.start_time >= :from
              AND w.start_time <= :to
            ORDER BY day ASC
        """;

        @SuppressWarnings("unchecked")
        List<Date> daysRows = entityManager
                .createNativeQuery(daysSql)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<OffsetDateTime> trainingDays = daysRows.stream()
                .map(d -> d.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC))
                .collect(toList());

        // Para streaks necesitamos las fechas hasta hoy (tomamos ventana amplia hasta 1 año hacia atrás).
        OffsetDateTime streakFrom = to.minusYears(1);
        @SuppressWarnings("unchecked")
        List<Date> streakRows = entityManager
                .createNativeQuery(daysSql)
                .setParameter("userId", userId)
                .setParameter("from", streakFrom)
                .setParameter("to", to)
                .getResultList();

        Set<java.time.LocalDate> streakSet = streakRows.stream()
                .map(Date::toLocalDate)
                .collect(Collectors.toSet());

        java.time.LocalDate toDate = to.toLocalDate();
        int currentStreak = 0;
        while (streakSet.contains(toDate.minusDays(currentStreak))) {
            currentStreak++;
        }

        // best streak: max consecutive length
        int bestStreak = 0;
        List<java.time.LocalDate> sorted = streakSet.stream().sorted().collect(toList());
        int run = 0;
        java.time.LocalDate prev = null;
        for (java.time.LocalDate d : sorted) {
            if (prev == null || !d.equals(prev.plusDays(1))) {
                run = 1;
            } else {
                run++;
            }
            bestStreak = Math.max(bestStreak, run);
            prev = d;
        }

        long daysInclusive = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;
        double avgDaysPerWeek = 0.0;
        if (daysInclusive > 0) {
            avgDaysPerWeek = trainingDays.size() / (daysInclusive / 7.0);
        }

        return new ConsistencyDTO(currentStreak, bestStreak, avgDaysPerWeek, trainingDays);
    }

    public DurationStatsDTO getDurationStats(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        String sql = """
            SELECT
              COALESCE(AVG(EXTRACT(EPOCH FROM (w.end_time - w.start_time)) / 60), 0) AS avgMinutes,
              COALESCE(MAX(EXTRACT(EPOCH FROM (w.end_time - w.start_time)) / 60), 0) AS longestMinutes
            FROM workouts w
            WHERE w.user_id = :userId
              AND w.start_time >= :from
              AND w.start_time <= :to
              AND w.end_time IS NOT NULL
        """;

        Object[] r = (Object[]) entityManager
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        int avgMinutes = (int) Math.round(toDouble(r[0]));
        int longestMinutes = (int) Math.round(toDouble(r[1]));
        return new DurationStatsDTO(avgMinutes, longestMinutes);
    }

    public VolumeDensityDTO getVolumeDensity(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime currentFrom = now.minusDays(30);
        OffsetDateTime previousFrom = now.minusDays(60);
        OffsetDateTime previousTo = currentFrom;

        String sql = """
            SELECT COALESCE(SUM(s.weight * s.reps), 0)
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId
              AND s.is_warmup = false
              AND w.start_time >= :from
              AND w.start_time <= :to
        """;

        double currentTotal = toDouble(entityManager
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("from", currentFrom)
                .setParameter("to", now)
                .getSingleResult());

        double previousTotal = toDouble(entityManager
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("from", previousFrom)
                .setParameter("to", previousTo)
                .getSingleResult());

        double currentDensity = currentTotal / 30.0;
        double previousDensity = previousTotal / 30.0;
        double changePercent = 0.0;
        if (previousDensity > 0) {
            changePercent = ((currentDensity - previousDensity) / previousDensity) * 100.0;
        }

        return new VolumeDensityDTO(currentDensity, previousDensity, changePercent);
    }

    public TrainingStyleDTO getTrainingStyle(UUID userId, OffsetDateTime from, OffsetDateTime to) {
        String sql = """
            SELECT
              SUM(CASE WHEN s.reps BETWEEN 1 AND 6 THEN 1 ELSE 0 END) AS strengthSets,
              SUM(CASE WHEN s.reps BETWEEN 7 AND 12 THEN 1 ELSE 0 END) AS hypertrophySets,
              SUM(CASE WHEN s.reps >= 13 THEN 1 ELSE 0 END) AS enduranceSets
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId
              AND s.is_warmup = false
              AND w.start_time >= :from
              AND w.start_time <= :to
        """;

        Object[] r = (Object[]) entityManager
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new TrainingStyleDTO(
                ((Number) r[0]).intValue(),
                ((Number) r[1]).intValue(),
                ((Number) r[2]).intValue()
        );
    }

    public WeeklyRhythmDTO getWeeklyRhythm(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime from = now.minusDays(6);

        String sql = """
            SELECT
              (EXTRACT(ISODOW FROM w.start_time)::int - 1) AS dayIndex,
              COUNT(*) AS sessions
            FROM workouts w
            WHERE w.user_id = :userId
              AND w.start_time >= :from
              AND w.start_time <= :to
            GROUP BY dayIndex
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery(sql, Object[].class)
                .setParameter("userId", userId)
                .setParameter("from", from)
                .setParameter("to", now)
                .getResultList();

        List<Integer> sessions = new ArrayList<>(Collections.nCopies(7, 0));
        for (Object[] r : rows) {
            int idx = ((Number) r[0]).intValue();
            int count = ((Number) r[1]).intValue();
            if (idx >= 0 && idx < 7) {
                sessions.set(idx, count);
            }
        }
        return new WeeklyRhythmDTO(sessions);
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof BigDecimal bd) return bd.doubleValue();
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(o.toString());
    }

    private static OffsetDateTime toOffsetDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof OffsetDateTime odt) return odt;
        if (o instanceof Timestamp ts) {
            return ts.toInstant().atOffset(ZoneOffset.UTC);
        }
        if (o instanceof java.time.LocalDateTime ldt) {
            return ldt.atOffset(ZoneOffset.UTC);
        }
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        // fallback
        return OffsetDateTime.parse(Objects.toString(o));
    }
}
