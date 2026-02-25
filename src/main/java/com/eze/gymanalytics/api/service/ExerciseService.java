package com.eze.gymanalytics.api.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.repository.ExerciseRepository;

@Service
public class ExerciseService {
  private final ExerciseRepository exerciseRepository;

  public ExerciseService(ExerciseRepository exerciseRepository) {
    this.exerciseRepository = exerciseRepository;
  }

  public List<Exercise> getAllExercises() {
    return exerciseRepository.findAll();
  }

  public List<Exercise> getExercisesByName(String name) {
    return exerciseRepository.findByNameContainingIgnoreCase(name);
  }
}
