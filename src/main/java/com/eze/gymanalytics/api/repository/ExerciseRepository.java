package com.eze.gymanalytics.api.repository;

import com.eze.gymanalytics.api.model.Exercise;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
  List<Exercise> findByNameContainingIgnoreCase(String name);

  /** Filtro por grupo muscular (case-insensitive). */
  List<Exercise> findByMuscleGroupIgnoreCase(String muscleGroup);

  /** Comprueba si ya existe un ejercicio con ese nombre exacto (para validar unicidad). */
  Optional<Exercise> findByNameIgnoreCase(String name);

  /** Comprueba unicidad excluyendo el propio ejercicio (para la operación PUT). */
  Optional<Exercise> findByNameIgnoreCaseAndIdNot(String name, Long id);

  /**
   * Devuelve todos los ejercicios ordenados por nombre (case-insensitive) a nivel de BD.
   * Se usa en AdminService en lugar de ordenar en memoria con un Comparator.
   */
  List<Exercise> findAllByOrderByNameAsc();
}
