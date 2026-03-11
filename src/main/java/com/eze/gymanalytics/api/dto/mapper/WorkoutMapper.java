package com.eze.gymanalytics.api.dto.mapper;

import org.springframework.stereotype.Component;

import com.eze.gymanalytics.api.dto.ExerciseInfoDTO;
import com.eze.gymanalytics.api.dto.SerieDTO;
import com.eze.gymanalytics.api.dto.WorkoutDTO;
import com.eze.gymanalytics.api.dto.WorkoutExerciseDTO;
import com.eze.gymanalytics.api.model.Serie;
import com.eze.gymanalytics.api.model.Workout;
import com.eze.gymanalytics.api.model.WorkoutExercise;

import java.util.stream.Collectors;

@Component
public class WorkoutMapper {

   public WorkoutDTO toDTO(Workout workout) {
      if (workout == null)
         return null;

      WorkoutDTO dto = new WorkoutDTO();
      dto.setId(workout.getId());
      dto.setName(workout.getName());
      dto.setStartTime(workout.getStartTime());
      dto.setEndTime(workout.getEndTime());
      dto.setNotes(workout.getNotes());
      dto.setTotalVolume(workout.getTotalVolume());

      // Solución BUG 1: Mapeo de la lista de ejercicios
      if (workout.getWorkoutExercises() != null) {
         dto.setExercises(
               workout.getWorkoutExercises().stream()
                     .map(this::mapWorkoutExercise)
                     .toList() // Usamos .toList() nativo de Java 16+
         );
      }

      return dto;
   }

   // Solución BUG 2: Construir el objeto anidado
   private WorkoutExerciseDTO mapWorkoutExercise(WorkoutExercise we) {
      if (we == null)
         return null;

      WorkoutExerciseDTO weDTO = new WorkoutExerciseDTO();
      weDTO.setId(we.getId());
      weDTO.setExerciseOrder(we.getExerciseOrder());
      weDTO.setNotes(we.getNotes());

      // Mapeamos la info del ejercicio anidado
      if (we.getExercise() != null) {
         ExerciseInfoDTO exInfo = new ExerciseInfoDTO();
         exInfo.setName(we.getExercise().getName());
         exInfo.setMuscleGroup(we.getExercise().getMuscleGroup());
         weDTO.setExercise(exInfo);
      }

      // Mapeamos las series
      if (we.getSeries() != null) {
         weDTO.setSeries(we.getSeries().stream().map(this::mapSerie).toList());
      }

      return weDTO;
   }

   private SerieDTO mapSerie(Serie serie) {
      if (serie == null)
         return null;

      SerieDTO sDTO = new SerieDTO();
      sDTO.setSetOrder(serie.getSetOrder());
      sDTO.setWeight(serie.getWeight());
      sDTO.setReps(serie.getReps());
      sDTO.setRpe(serie.getRpe());
      sDTO.setIsWarmup(serie.getIsWarmup());
      return sDTO;
   }
}