package com.eze.gymanalytics.api.service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.eze.gymanalytics.api.dto.ExerciseInfoDTO;
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

  public List<Exercise> getExercisesByMuscleGroup(String muscleGroup) {
    return exerciseRepository.findByMuscleGroupIgnoreCase(muscleGroup);
  }

  public List<Exercise> getExercisesByEquipment(String equipment) {
    return exerciseRepository.findByEquipmentIgnoreCase(equipment);
  }

  public List<ExerciseInfoDTO> getExercisesFiltered(String muscleGroup, String equipment, String name, int page, int size) {
    Specification<Exercise> spec = Specification.where(null);

    if (muscleGroup != null) {
      spec = spec.and((root, query, cb) ->
        cb.equal(cb.lower(root.get("muscleGroup")), muscleGroup.toLowerCase()));
    }
    if (equipment != null) {
      spec = spec.and((root, query, cb) ->
        cb.equal(cb.lower(root.get("equipment")), equipment.toLowerCase()));
    }
    if (name != null && !name.isBlank()) {
      spec = spec.and((root, query, cb) ->
        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
    }

    return exerciseRepository.findAll(spec, PageRequest.of(page, size))
      .stream()
      .map(this::toInfoDTO)
      .toList();
  }

  private ExerciseInfoDTO toInfoDTO(Exercise e) {
    ExerciseInfoDTO dto = new ExerciseInfoDTO();
    dto.setId(e.getId());
    dto.setName(e.getName());
    dto.setMuscleGroup(e.getMuscleGroup());
    dto.setThumbnailUrl(e.getThumbnailUrl());
    dto.setVideoUrl(e.getVideoUrl());
    return dto;
  }
}
