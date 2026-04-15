package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.routines.*;
import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.model.Routine;
import com.eze.gymanalytics.api.model.RoutineExercise;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.RoutineExerciseRepository;
import com.eze.gymanalytics.api.repository.RoutineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

  private final RoutineRepository routineRepository;
  private final RoutineExerciseRepository routineExerciseRepository;
  private final ExerciseRepository exerciseRepository;
  private final ProfileRepository profileRepository;

  public RoutineController(
      RoutineRepository routineRepository,
      RoutineExerciseRepository routineExerciseRepository,
      ExerciseRepository exerciseRepository,
      ProfileRepository profileRepository) {
    this.routineRepository = routineRepository;
    this.routineExerciseRepository = routineExerciseRepository;
    this.exerciseRepository = exerciseRepository;
    this.profileRepository = profileRepository;
  }

  private UUID requireUserId(String email) {
    return profileRepository.findByEmail(email)
        .map(Profile::getId)
        .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado para email: " + email));
  }

  @GetMapping
  public ResponseEntity<List<RoutineDTO>> getRoutines(@AuthenticationPrincipal String email) {
    UUID userId = requireUserId(email);
    List<Routine> routines = routineRepository.findByUserId(userId);
    List<RoutineDTO> out = routines.stream().map(this::toDTO).collect(Collectors.toList());
    return ResponseEntity.ok(out);
  }

  @PostMapping
  public ResponseEntity<RoutineDTO> createRoutine(
      @RequestBody RoutineCreateRequest request,
      @AuthenticationPrincipal String email) {
    UUID userId = requireUserId(email);
    Profile user = profileRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));

    Routine routine = new Routine();
    routine.setUser(user);
    routine.setName(request.getName());
    routine.setDescription(request.getDescription());
    Routine saved = routineRepository.save(routine);

    List<RoutineExercise> exercises = (request.getExercises() == null ? List.<RoutineExerciseCreateRequest>of() : request.getExercises())
        .stream()
        .map(req -> {
          Exercise ex = exerciseRepository.findById(req.getExerciseId())
              .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + req.getExerciseId()));
          RoutineExercise re = new RoutineExercise();
          re.setRoutine(saved);
          re.setExercise(ex);
          re.setExerciseOrder(req.getExerciseOrder());
          re.setTargetSeries(req.getTargetSeries());
          re.setNotes(null);
          return re;
        })
        .collect(Collectors.toList());

    List<RoutineExercise> savedExercises = routineExerciseRepository.saveAll(exercises);

    RoutineDTO dto = this.toDTO(saved);
    // asegurar el orden/visibilidad de ejercicios
    dto.setExercises(savedExercises.stream().map(this::toExerciseDTO).collect(Collectors.toList()));
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRoutine(
      @PathVariable Long id,
      @AuthenticationPrincipal String email) {
    UUID userId = requireUserId(email);
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Rutina no encontrada"));

    if (!routine.getUser().getId().equals(userId)) {
      return ResponseEntity.status(403).build();
    }

    routineRepository.delete(routine);
    return ResponseEntity.ok().build();
  }

  private RoutineDTO toDTO(Routine routine) {
    RoutineDTO dto = new RoutineDTO();
    dto.setId(routine.getId());
    dto.setName(routine.getName());
    dto.setDescription(routine.getDescription());

    if (routine.getRoutineExercises() != null) {
      dto.setExercises(
          routine.getRoutineExercises().stream()
              .map(this::toExerciseDTO)
              .collect(Collectors.toList()));
    } else {
      dto.setExercises(List.of());
    }

    return dto;
  }

  private RoutineExerciseDTO toExerciseDTO(RoutineExercise re) {
    RoutineExerciseDTO dto = new RoutineExerciseDTO();
    dto.setId(re.getId());
    dto.setExerciseId(re.getExercise() != null ? re.getExercise().getId() : null);
    dto.setExerciseOrder(re.getExerciseOrder());
    dto.setExerciseName(re.getExercise() != null ? re.getExercise().getName() : null);
    dto.setMuscleGroup(re.getExercise() != null ? re.getExercise().getMuscleGroup() : null);
    dto.setThumbnailUrl(re.getExercise() != null ? re.getExercise().getThumbnailUrl() : null);
    dto.setNotes(re.getNotes());
    dto.setTargetSeries(re.getTargetSeries());
    return dto;
  }
}
