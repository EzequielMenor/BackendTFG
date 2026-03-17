package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.dto.analytics.*;
import com.eze.gymanalytics.api.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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
                (s.weight * (36.0 / (37.0 - s.reps))) as current_1rm,
                RANK() OVER (PARTITION BY e.id ORDER BY (s.weight * (36.0 / (37.0 - s.reps))) DESC, w.start_time ASC) as rank
            FROM series s
            JOIN workout_exercises we ON s.workout_exercise_id = we.id
            JOIN exercises e ON we.exercise_id = e.id
            JOIN workouts w ON we.workout_id = w.id
            WHERE w.user_id = :userId AND s.is_warmup = false AND s.reps <= 12 AND s.weight > 0
        )
        SELECT exercise_id, exercise_name, pr_date, current_1rm
        FROM exercise_prs
        WHERE rank = 1 AND pr_date BETWEEN :from AND :to
        ORDER BY pr_date DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findRecentPRsNative(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
