package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.model.*;
import com.eze.gymanalytics.api.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImportService {

  private final WorkoutRepository workoutRepository;
  private final ExerciseRepository exerciseRepository;
  private final ProfileRepository profileRepository;
  private final WorkoutExerciseRepository workoutExerciseRepository;
  private final SerieRepository serieRepository;

  public ImportService(WorkoutRepository workoutRepository,
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

  private static final DateTimeFormatter HEVY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm",
      Locale.ENGLISH);

  @Transactional
  public void importHevyCsv(MultipartFile file, String userEmail) {
    Profile user = profileRepository.findByEmail(userEmail)
        .orElseGet(() -> {
          Profile newProfile = new Profile();
          newProfile.setId(UUID.randomUUID());
          newProfile.setEmail(userEmail);
          newProfile.setUsername(userEmail.split("@")[0]);
          newProfile.setRole("user");
          return profileRepository.save(newProfile);
        });

    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

      Map<String, Workout> workoutMap = new HashMap<>();
      Map<String, WorkoutExercise> workoutExerciseMap = new HashMap<>();
      List<Serie> allSeries = new ArrayList<>();

      for (CSVRecord record : csvParser) {
        String startTimeStr = record.get("start_time");
        String exerciseName = record.get("exercise_title");

        // 1. WORKOUT (Iniciamos volumen a 0)
        Workout workout = workoutMap.computeIfAbsent(startTimeStr, k -> {
          Workout w = createWorkout(record, user);
          w.setTotalVolume(BigDecimal.ZERO);
          return workoutRepository.save(w);
        });

        // 2. WORKOUT_EXERCISE
        String weKey = startTimeStr + exerciseName;
        WorkoutExercise we = workoutExerciseMap.computeIfAbsent(weKey, k -> {
          Exercise exercise = exerciseRepository.findByNameContainingIgnoreCase(exerciseName)
              .stream().findFirst()
              .orElseGet(() -> {
                Exercise newEx = new Exercise();
                newEx.setName(exerciseName);
                newEx.setMuscleGroup("Otros");
                newEx.setDescription("Creado automáticamente");
                return exerciseRepository.save(newEx);
              });

          WorkoutExercise newWe = new WorkoutExercise();
          newWe.setWorkout(workout);
          newWe.setExercise(exercise);
          newWe.setExerciseOrder(workoutExerciseMap.size() + 1);
          newWe.setNotes(record.get("exercise_notes"));
          return workoutExerciseRepository.save(newWe);
        });

        // 3. SERIE
        Serie serie = new Serie();
        serie.setWorkoutExercise(we);
        serie.setSetOrder(Integer.parseInt(record.get("set_index")));
        serie.setWeight(parseBigDecimal(record.get("weight_kg")));
        serie.setReps(parseInteger(record.get("reps")));
        serie.setRpe(parseBigDecimal(record.get("rpe")));
        serie.setIsWarmup(record.get("set_type").equalsIgnoreCase("warmup"));

        // CALCULAR VOLUMEN Y SUMAR AL WORKOUT
        BigDecimal serieVolume = serie.getWeight().multiply(new BigDecimal(serie.getReps()));
        workout.setTotalVolume(workout.getTotalVolume().add(serieVolume));

        allSeries.add(serie);
      }

      // Guardamos las series y actualizamos los entrenamientos con el volumen final
      serieRepository.saveAll(allSeries);
      workoutRepository.saveAll(workoutMap.values());

    } catch (Exception e) {
      throw new RuntimeException("Error al procesar el CSV: " + e.getMessage());
    }
  }

  private Workout createWorkout(CSVRecord record, Profile user) {
    LocalDateTime localDateTime = LocalDateTime.parse(record.get("start_time"), HEVY_DATE_FORMATTER);
    Workout w = new Workout();
    w.setUser(user);
    w.setName(record.get("title"));
    w.setStartTime(localDateTime.atOffset(ZoneOffset.UTC));
    w.setNotes(record.get("description"));
    return w;
  }

  private BigDecimal parseBigDecimal(String val) {
    return (val == null || val.isEmpty()) ? BigDecimal.ZERO : new BigDecimal(val);
  }

  private Integer parseInteger(String val) {
    return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val);
  }
}
