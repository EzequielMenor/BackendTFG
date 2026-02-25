package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.SerieDTO;
import com.eze.gymanalytics.api.dto.WorkoutDTO;
import com.eze.gymanalytics.api.dto.WorkoutExerciseDTO;
import com.eze.gymanalytics.api.model.Exercise;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.model.Serie;
import com.eze.gymanalytics.api.model.Workout;
import com.eze.gymanalytics.api.model.WorkoutExercise;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.SerieRepository;
import com.eze.gymanalytics.api.repository.WorkoutExerciseRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final ProfileRepository profileRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final SerieRepository serieRepository;

    public WorkoutService(WorkoutRepository workoutRepository,
                          ExerciseRepository exerciseRepository,
                          ProfileRepository profileRepository,
                          WorkoutExerciseRepository workoutExerciseRepository,
                          SerieRepository serieRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.profileRepository = profileRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.serieRepository = serieRepository;
    }

    @Transactional
    public WorkoutDTO createWorkout(WorkoutDTO workoutDTO, String userEmail) {
        Profile user = profileRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(workoutDTO.getName());
        workout.setStartTime(workoutDTO.getStartTime());
        workout.setEndTime(workoutDTO.getEndTime());
        workout.setNotes(workoutDTO.getNotes());
        workout.setTotalVolume(BigDecimal.ZERO);

        Workout savedWorkout = workoutRepository.save(workout);

        if (workoutDTO.getExercises() != null && !workoutDTO.getExercises().isEmpty()) {
            List<WorkoutExercise> weListToSave = new ArrayList<>();
            for (WorkoutExerciseDTO exerciseDTO : workoutDTO.getExercises()) {
                
                Exercise exercise = exerciseRepository.findById(exerciseDTO.getExerciseId())
                        .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

                WorkoutExercise we = new WorkoutExercise();
                we.setWorkout(savedWorkout);
                we.setExercise(exercise);
                we.setExerciseOrder(exerciseDTO.getExerciseOrder());
                we.setNotes(exerciseDTO.getNotes());
                
                WorkoutExercise savedWe = workoutExerciseRepository.save(we);
                weListToSave.add(savedWe);

                if (exerciseDTO.getSeries() != null && !exerciseDTO.getSeries().isEmpty()) {
                    List<Serie> seriesToSave = new ArrayList<>();
                    
                    for (SerieDTO serieDTO : exerciseDTO.getSeries()) {
                        Serie serie = new Serie();
                        serie.setWorkoutExercise(savedWe);
                        serie.setWeight(serieDTO.getWeight());
                        serie.setReps(serieDTO.getReps());
                        serie.setRpe(serieDTO.getRpe());
                        serie.setIsWarmup(serieDTO.getIsWarmup());
                        serie.setSetOrder(serieDTO.getSetOrder());
                        
                        if (serie.getWeight() != null && serie.getReps() != null) {
                            BigDecimal serieVolume = serie.getWeight().multiply(new BigDecimal(serie.getReps()));
                            savedWorkout.setTotalVolume(savedWorkout.getTotalVolume().add(serieVolume));
                        }

                        seriesToSave.add(serie);
                    }
                    serieRepository.saveAll(seriesToSave);
                    savedWe.setSeries(seriesToSave); 
                }
            }
            savedWorkout.setWorkoutExercises(weListToSave);
            workoutRepository.save(savedWorkout);
        }

        return convertToDTO(savedWorkout);
    }

    public Page<WorkoutDTO> getUserWorkouts(String userEmail, Pageable pageable, OffsetDateTime startDate, OffsetDateTime endDate) {
        Profile user = profileRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        Page<Workout> workoutsPage;

        if (startDate != null && endDate != null) {
            workoutsPage = workoutRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
                    user.getId(), startDate, endDate, pageable);
        } else {
            workoutsPage = workoutRepository.findByUserIdOrderByStartTimeDesc(
                    user.getId(), pageable);
        }

        return workoutsPage.map(this::convertToDTO);
    }

    public void deleteWorkout(Long id, String userEmail) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout no encontrado"));
                
        if (!workout.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("No tienes permiso para borrar este workout");
        }
        
        workoutRepository.delete(workout);
    }

    private WorkoutDTO convertToDTO(Workout workout) {
        WorkoutDTO dto = new WorkoutDTO();
        dto.setId(workout.getId());
        dto.setName(workout.getName());
        dto.setStartTime(workout.getStartTime());
        dto.setEndTime(workout.getEndTime());
        dto.setNotes(workout.getNotes());
        dto.setTotalVolume(workout.getTotalVolume());
        
        if(workout.getWorkoutExercises() != null) {
           List<WorkoutExerciseDTO> exerciseDTOs = workout.getWorkoutExercises().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
           dto.setExercises(exerciseDTOs);
        }

        return dto;
    }

    private WorkoutExerciseDTO convertToDTO(WorkoutExercise we) {
        WorkoutExerciseDTO dto = new WorkoutExerciseDTO();
        dto.setId(we.getId());
        dto.setExerciseId(we.getExercise().getId());
        dto.setExerciseName(we.getExercise().getName());
        dto.setExerciseOrder(we.getExerciseOrder());
        dto.setNotes(we.getNotes());

        if(we.getSeries() != null) {
            List<SerieDTO> seriesDTOs = we.getSeries().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            dto.setSeries(seriesDTOs);
        }
        
        return dto;
    }

    private SerieDTO convertToDTO(Serie serie) {
        SerieDTO dto = new SerieDTO();
        dto.setId(serie.getId());
        dto.setWeight(serie.getWeight());
        dto.setReps(serie.getReps());
        dto.setRpe(serie.getRpe());
        dto.setIsWarmup(serie.getIsWarmup());
        dto.setSetOrder(serie.getSetOrder());
        return dto;
    }
}
