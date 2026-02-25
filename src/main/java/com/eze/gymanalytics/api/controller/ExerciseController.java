package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.service.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

  private final ExerciseService exerciseService;

  public ExerciseController(ExerciseService exerciseService) {
    this.exerciseService = exerciseService;
  }

  @GetMapping
  public ResponseEntity<List<Exercise>> getAllExercises() {
    return ResponseEntity.ok(exerciseService.getAllExercises());
  }

  @GetMapping("/search")
  public ResponseEntity<List<Exercise>> searchExercises(@RequestParam String name) {
    return ResponseEntity.ok(exerciseService.getExercisesByName(name));
  }
}
