package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    // Grupo muscular más entrenado en un rango de fechas
    @Query(value = "SELECT e.muscle_group FROM workout_exercises we JOIN exercises e ON we.exercise_id = e.id JOIN workouts w ON we.workout_id = w.id WHERE w.user_id = :userId AND w.start_time >= :from AND w.start_time <= :to GROUP BY e.muscle_group ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String findTopMuscleGroupByUserAndDateRange(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}