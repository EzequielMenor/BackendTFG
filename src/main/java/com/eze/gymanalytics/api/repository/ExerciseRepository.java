package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Exercise;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long>, JpaSpecificationExecutor<Exercise> {
  List<Exercise> findByNameContainingIgnoreCase(String name);
  List<Exercise> findByMuscleGroupIgnoreCase(String muscleGroup);
  List<Exercise> findByEquipmentIgnoreCase(String equipment);

  @Query(value = "SELECT * FROM exercises WHERE :name = ANY(aliases) LIMIT 1", nativeQuery = true)
  Optional<Exercise> findByAlias(@Param("name") String name);
}
