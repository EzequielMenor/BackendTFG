package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
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

    // Usuarios activos (con al menos un workout) en los últimos N días
    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM workouts WHERE start_time >= :since", nativeQuery = true)
    long countDistinctActiveUsersSince(@Param("since") OffsetDateTime since);
}
