package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    
    // Paginación y ordenación por fecha descendente
    Page<Workout> findByUserIdOrderByStartTimeDesc(UUID userId, Pageable pageable);

    // Filtro por rango de fechas + Paginación
    Page<Workout> findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
            UUID userId, 
            OffsetDateTime start, 
            OffsetDateTime end, 
            Pageable pageable);
}
