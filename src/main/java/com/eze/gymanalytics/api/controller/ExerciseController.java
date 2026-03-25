package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.ExerciseInfoDTO;
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
  public ResponseEntity<List<ExerciseInfoDTO>> getAllExercises(
      @RequestParam(required = false) String muscleGroup,
      @RequestParam(required = false) String equipment,
      @RequestParam(required = false) String name,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(exerciseService.getExercisesFiltered(muscleGroup, equipment, name, page, size));
  }

  @GetMapping("/search")
  public ResponseEntity<List<Exercise>> searchExercises(@RequestParam String name) {
    return ResponseEntity.ok(exerciseService.getExercisesByName(name));
  }

  @GetMapping("/by-muscle")
  public ResponseEntity<List<Exercise>> getByMuscle(@RequestParam String muscleGroup) {
    return ResponseEntity.ok(exerciseService.getExercisesByMuscleGroup(muscleGroup));
  }

  @GetMapping("/by-equipment")
  public ResponseEntity<List<Exercise>> getByEquipment(@RequestParam String equipment) {
    return ResponseEntity.ok(exerciseService.getExercisesByEquipment(equipment));
  }
}
