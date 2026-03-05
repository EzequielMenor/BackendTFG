package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO;
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
}
