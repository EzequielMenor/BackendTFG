package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.dto.analytics.DurationStatsResponse;
import com.eze.gymanalytics.api.model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    // Paginación y ordenación por fecha descendente
    Page<Workout> findByUserIdOrderByStartTimeDesc(UUID userId, Pageable pageable);

    Optional<Workout> findByIdAndUserEmail(Long id, String email);

    // Filtro por rango de fechas + Paginación
    Page<Workout> findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable);

    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user.id = :userId AND w.startTime BETWEEN :from AND :to")
    long countByUserIdAndStartTimeBetween(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("SELECT SUM(w.totalVolume) FROM Workout w WHERE w.user.id = :userId AND w.startTime BETWEEN :from AND :to")
    BigDecimal sumTotalVolumeByUserIdAndStartTimeBetween(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("SELECT w.startTime FROM Workout w WHERE w.user.id = :userId AND w.startTime BETWEEN :from AND :to ORDER BY w.startTime ASC")
    List<OffsetDateTime> findTrainingDaysByUserIdAndRange(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query(value = """
        SELECT 
            CAST(AVG(EXTRACT(EPOCH FROM (w.end_time - w.start_time)) / 60) AS INTEGER) as avg_min,
            CAST(MAX(EXTRACT(EPOCH FROM (w.end_time - w.start_time)) / 60) AS INTEGER) as max_min
        FROM workouts w
        WHERE w.user_id = :userId 
          AND w.start_time BETWEEN :from AND :to 
          AND w.end_time IS NOT NULL
    """, nativeQuery = true)
    List<Object[]> findDurationStatsNative(@Param("userId") UUID userId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
