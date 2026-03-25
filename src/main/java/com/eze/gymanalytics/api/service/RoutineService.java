package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.RoutineDTO;
import com.eze.gymanalytics.api.dto.RoutineExerciseDTO;
import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.model.Routine;
import com.eze.gymanalytics.api.model.RoutineExercise;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.RoutineRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ProfileRepository profileRepository;
    private final ExerciseRepository exerciseRepository;

    public RoutineService(RoutineRepository routineRepository,
                          ProfileRepository profileRepository,
                          ExerciseRepository exerciseRepository) {
        this.routineRepository = routineRepository;
        this.profileRepository = profileRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<RoutineDTO> getUserRoutines(String email) {
        Profile user = profileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Routine> routines = routineRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return routines.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public RoutineDTO createRoutine(RoutineDTO routineDTO, String email) {
        Profile user = profileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Routine routine = new Routine();
        routine.setUser(user);
        routine.setName(routineDTO.getName());
        routine.setDescription(routineDTO.getDescription());

        Routine savedRoutine = routineRepository.save(routine);

        List<RoutineExercise> exerciseList = new ArrayList<>();
        if (routineDTO.getExercises() != null) {
            for (RoutineExerciseDTO exDTO : routineDTO.getExercises()) {
                Exercise exercise = exerciseRepository.findById(exDTO.getExerciseId())
                        .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado: " + exDTO.getExerciseId()));

                RoutineExercise re = new RoutineExercise();
                re.setRoutine(savedRoutine);
                re.setExercise(exercise);
                re.setExerciseOrder(exDTO.getExerciseOrder() != null ? exDTO.getExerciseOrder() : exerciseList.size() + 1);
                re.setTargetSeries(exDTO.getTargetSeries());
                re.setNotes(exDTO.getNotes());
                exerciseList.add(re);
            }
        }
        savedRoutine.setRoutineExercises(exerciseList);
        Routine finalRoutine = routineRepository.save(savedRoutine);
        return convertToDTO(finalRoutine);
    }

    public void deleteRoutine(Long id, String email) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        if (!routine.getUser().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso para borrar esta rutina");
        }

        routineRepository.delete(routine);
    }

    private RoutineDTO convertToDTO(Routine routine) {
        RoutineDTO dto = new RoutineDTO();
        dto.setId(routine.getId());
        dto.setName(routine.getName());
        dto.setDescription(routine.getDescription());

        if (routine.getRoutineExercises() != null) {
            List<RoutineExerciseDTO> exDTOs = routine.getRoutineExercises().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dto.setExercises(exDTOs);
        }

        return dto;
    }

    private RoutineExerciseDTO convertToDTO(RoutineExercise re) {
        RoutineExerciseDTO dto = new RoutineExerciseDTO();
        dto.setId(re.getId());
        dto.setExerciseOrder(re.getExerciseOrder());
        dto.setTargetSeries(re.getTargetSeries());
        dto.setNotes(re.getNotes());
        if (re.getExercise() != null) {
            dto.setExerciseId(re.getExercise().getId());
            dto.setExerciseName(re.getExercise().getName());
            dto.setMuscleGroup(re.getExercise().getMuscleGroup());
            dto.setThumbnailUrl(re.getExercise().getThumbnailUrl());
        }
        return dto;
    }
}
