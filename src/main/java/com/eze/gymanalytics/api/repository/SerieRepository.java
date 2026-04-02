package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.dto.analytics.*;
import com.eze.gymanalytics.api.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    @Query("""
        SELECT new com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO(
            w.startTime,
            MAX(s.weight * (36.0 / (37.0 - s.reps)))
        )
        FROM Serie s
        JOIN s.workoutExercise we
        JOIN we.workout w
        WHERE we.exercise.id = :exerciseId
          AND w.user.id = :userId
          AND s.isWarmup = false
          AND s.reps <= 12
        GROUP BY w.id, w.startTime
        ORDER BY w.startTime ASC
    """)
    List<Progression1RMDTO> find1RMProgression(
            @Param("userId") UUID userId, 
            @Param("exerciseId") Long exerciseId);

    @Query("""
        SELECT new com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO(
            we.exercise.muscleGroup,
            COUNT(s.id)
        )
        FROM Serie s
        JOIN s.workoutExercise we
        JOIN we.workout w
        WHERE w.user.id = :userId
          AND s.isWarmup = false
          AND (s.rpe IS NULL OR s.rpe >= 7.0)
          AND w.startTime >= :startDate
        GROUP BY we.exercise.muscleGroup
    """)
    List<EffectiveVolumeDTO> findEffectiveVolume(
            @Param("userId") UUID userId, 
            @Param("startDate") OffsetDateTime startDate);

    @Query(value = """
        SELECT DATE_TRUNC('week', w.start_time) as weekStart, SUM(w.total_volume) as totalVolume
        FROM workouts w
        WHERE w.user_id = :userId AND w.start_time BETWEEN :from AND :to
        GROUP BY weekStart
        ORDER BY weekStart ASC
    """, nativeQuery = true)
    List<Object[]> findWeeklyVolumeNative(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query(value = """
        SELECT e.muscle_group, COUNT(s.id) as sets_count
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN exercises e ON we.exercise_id = e.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId AND w.start_time BETWEEN :from AND :to
        GROUP BY e.muscle_group
    """, nativeQuery = true)
    List<Object[]> findMuscleDistributionNative(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query(value = """
        SELECT e.id, e.name, MAX(s.weight * (36.0 / (37.0 - s.reps))) as best1rm
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN exercises e ON we.exercise_id = e.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId AND s.is_warmup = false AND s.reps <= 12 AND s.weight > 0
        GROUP BY e.id, e.name
        ORDER BY best1rm DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopExercisesNative(@Param("userId") UUID userId, @Param("limit") int limit);

    @Query(value = """
        WITH exercise_prs AS (
            SELECT
                e.id as exercise_id,
                e.name as exercise_name,
                w.start_time as pr_date,
                s.weight as max_weight,
                ROW_NUMBER() OVER (
                    PARTITION BY e.id
                    ORDER BY s.weight DESC, w.start_time DESC, s.id DESC
                ) as rn
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN exercises e ON we.exercise_id = e.id
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId AND s.is_warmup = false AND s.weight > 0
        )
        SELECT exercise_id, exercise_name, pr_date, max_weight
        FROM exercise_prs
        WHERE rn = 1 AND pr_date BETWEEN :from AND :to
        ORDER BY pr_date DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findRecentPRsNative(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    // ── EZE-167: PR detection ──────────────────────────────────────────────────

    @Query("""
        SELECT MAX(s.weight)
        FROM Serie s
        JOIN s.workoutExercise we
        JOIN we.workout w
        WHERE w.user.id = :userId
          AND we.exercise.id = :exerciseId
          AND w.id != :excludeWorkoutId
          AND s.isWarmup = false
    """)
    Optional<BigDecimal> findHistoricalMaxWeight(
            @Param("userId") UUID userId,
            @Param("exerciseId") Long exerciseId,
            @Param("excludeWorkoutId") Long excludeWorkoutId);

    @Query("""
        SELECT MAX(s.reps)
        FROM Serie s
        JOIN s.workoutExercise we
        JOIN we.workout w
        WHERE w.user.id = :userId
          AND we.exercise.id = :exerciseId
          AND w.id != :excludeWorkoutId
          AND s.weight = :weight
          AND s.isWarmup = false
    """)
    Optional<Integer> findHistoricalMaxRepsAtWeight(
            @Param("userId") UUID userId,
            @Param("exerciseId") Long exerciseId,
            @Param("excludeWorkoutId") Long excludeWorkoutId,
            @Param("weight") BigDecimal weight);

    // ── EZE-168: Volume Density ────────────────────────────────────────────────

    @Query(value = """
        SELECT COALESCE(SUM(s.weight * s.reps), 0) / NULLIF(COUNT(s.id), 0)
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId
          AND w.start_time BETWEEN :from AND :to
          AND s.is_warmup = false
    """, nativeQuery = true)
    Double findVolumeDensity(@Param("userId") UUID userId,
                             @Param("from") OffsetDateTime from,
                             @Param("to") OffsetDateTime to);

    // ── EZE-168: Training Style ────────────────────────────────────────────────

    @Query(value = """
        SELECT
            SUM(CASE WHEN s.reps <= 5 THEN 1 ELSE 0 END)  as strength,
            SUM(CASE WHEN s.reps BETWEEN 6 AND 12 THEN 1 ELSE 0 END) as hypertrophy,
            SUM(CASE WHEN s.reps > 12 THEN 1 ELSE 0 END)  as endurance
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId
          AND w.start_time BETWEEN :from AND :to
          AND s.is_warmup = false
    """, nativeQuery = true)
    List<Object[]> findTrainingStyleCounts(@Param("userId") UUID userId,
                                           @Param("from") OffsetDateTime from,
                                           @Param("to") OffsetDateTime to);

    // ── EZE-169: Exercise Trend ────────────────────────────────────────────────

    @Query(value = """
        SELECT w.start_time, AVG(s.weight * s.reps) as avg_volume
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId
          AND we.exercise_id = :exerciseId
          AND s.is_warmup = false
        GROUP BY w.id, w.start_time
        ORDER BY w.start_time DESC
        LIMIT :sessionLimit
    """, nativeQuery = true)
    List<Object[]> findRecentExerciseSessions(
            @Param("userId") UUID userId,
            @Param("exerciseId") Long exerciseId,
            @Param("sessionLimit") int sessionLimit);

    @Query(value = """
        SELECT w.start_time, AVG(s.weight * s.reps) as avg_volume
        FROM series s
        JOIN workout_exercises we ON s.workout_exercise_id = we.id
        JOIN workouts w ON we.workout_id = w.id
        WHERE w.user_id = :userId
          AND we.exercise_id = :exerciseId
          AND s.is_warmup = false
          AND w.start_time BETWEEN :from AND :to
        GROUP BY w.id, w.start_time
        ORDER BY w.start_time DESC
        LIMIT :sessionLimit
    """, nativeQuery = true)
    List<Object[]> findExerciseSessionsInRange(
            @Param("userId") UUID userId,
            @Param("exerciseId") Long exerciseId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("sessionLimit") int sessionLimit);
}
