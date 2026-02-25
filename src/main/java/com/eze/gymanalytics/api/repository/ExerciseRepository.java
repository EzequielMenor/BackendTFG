package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Exercise;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
  List<Exercise> findByNameContainingIgnoreCase(String name);
}
