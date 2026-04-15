package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.ImportResultDTO;
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

  private static final DateTimeFormatter HEVY_DATE_FORMATTER_ES = new java.time.format.DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern("d MMM yyyy, HH:mm")
      .toFormatter(new Locale("es"));

  private static final DateTimeFormatter HEVY_DATE_FORMATTER_ES_2 = new java.time.format.DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern("dd MMM yyyy, HH:mm")
      .toFormatter(new Locale("es"));

  // Tu CSV viene con meses en inglés (Apr, Mar, etc.)
  private static final DateTimeFormatter HEVY_DATE_FORMATTER_EN = new java.time.format.DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern("d MMM yyyy, HH:mm")
      .toFormatter(Locale.ENGLISH);

  private static final DateTimeFormatter HEVY_DATE_FORMATTER_EN_2 = new java.time.format.DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern("dd MMM yyyy, HH:mm")
      .toFormatter(Locale.ENGLISH);

  @Transactional
  public ImportResultDTO importHevyCsv(MultipartFile file, String userEmail) {
    Profile user = profileRepository.findByEmail(userEmail)
        .orElseGet(() -> {
          Profile newProfile = new Profile();
          newProfile.setId(UUID.randomUUID());
          newProfile.setEmail(userEmail);
          newProfile.setUsername(userEmail.split("@")[0]);
          newProfile.setRole("user");
          return profileRepository.save(newProfile);
        });

    // Pre-cargar todos los ejercicios en un Map para evitar N queries al buscar por nombre.
    // Clave: nombre normalizado (lowercase); Valor: entidad Exercise.
    Map<String, Exercise> exerciseCache = new HashMap<>();
    exerciseRepository.findAll().forEach(ex ->
        exerciseCache.put(ex.getName().toLowerCase(), ex)
    );

    List<String> failedRows = new ArrayList<>();
    int successCount = 0;

    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser csvParser = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

      Map<String, Workout> workoutMap = new HashMap<>();
      Map<String, WorkoutExercise> workoutExerciseMap = new HashMap<>();
      List<Serie> allSeries = new ArrayList<>();

      for (CSVRecord record : csvParser) {
        try {
          String startTimeStr = record.get("start_time");
          String exerciseName = record.get("exercise_title");

          // 1. WORKOUT
          Workout workout = workoutMap.computeIfAbsent(startTimeStr, k -> {
            Workout w = createWorkout(record, user);
            w.setTotalVolume(BigDecimal.ZERO);
            return workoutRepository.save(w);
          });

          // 2. WORKOUT_EXERCISE — resolución de ejercicio desde cache (sin query extra)
          String weKey = startTimeStr + exerciseName;
          WorkoutExercise we = workoutExerciseMap.computeIfAbsent(weKey, k -> {
            Exercise exercise = exerciseCache.computeIfAbsent(
                exerciseName.toLowerCase(),
                name -> {
                  Exercise newEx = new Exercise();
                  newEx.setName(exerciseName);
                  newEx.setMuscleGroup("Otros");
                  newEx.setDescription("Creado automáticamente");
                  return exerciseRepository.save(newEx);
                }
            );

            WorkoutExercise newWe = new WorkoutExercise();
            newWe.setWorkout(workout);
            newWe.setExercise(exercise);
            newWe.setExerciseOrder(workoutExerciseMap.size() + 1);
            newWe.setNotes(record.get("exercise_notes"));
            return newWe;
          });

          // 3. SERIE
          Serie serie = new Serie();
          serie.setWorkoutExercise(we);
          serie.setSetOrder(Integer.parseInt(record.get("set_index")));
          serie.setWeight(parseBigDecimal(record.get("weight_kg")));
          serie.setReps(parseInteger(record.get("reps")));
          serie.setRpe(parseBigDecimal(record.get("rpe")));
          serie.setIsWarmup(record.get("set_type").equalsIgnoreCase("warmup"));

          BigDecimal serieVolume = serie.getWeight().multiply(new BigDecimal(serie.getReps()));
          workout.setTotalVolume(workout.getTotalVolume().add(serieVolume));

          allSeries.add(serie);
          successCount++;

        } catch (Exception e) {
          // Una fila mal formada no aborta la importación completa
          failedRows.add("Fila " + record.getRecordNumber() + ": " + e.getMessage());
        }
      }

      // Batch save: series en lotes de 500 según config de Hibernate
      // Persistir primero workout_exercises (para que series tenga FK estable)
      workoutExerciseRepository.saveAll(workoutExerciseMap.values());
      // Luego series
      serieRepository.saveAll(allSeries);
      // Actualizar volumen total de todos los workouts
      workoutRepository.saveAll(workoutMap.values());

    } catch (Exception e) {
      throw new RuntimeException("Error al leer el CSV: " + e.getMessage());
    }

    return new ImportResultDTO(successCount, failedRows.size(), failedRows);
  }

  private Workout createWorkout(CSVRecord record, Profile user) {
    String raw = record.get("start_time");
    LocalDateTime localDateTime;
    try {
      localDateTime = LocalDateTime.parse(raw, HEVY_DATE_FORMATTER_EN);
    } catch (Exception ignored) {
      try {
        localDateTime = LocalDateTime.parse(raw, HEVY_DATE_FORMATTER_EN_2);
      } catch (Exception ignored2) {
        try {
          localDateTime = LocalDateTime.parse(raw, HEVY_DATE_FORMATTER_ES);
        } catch (Exception ignored3) {
          localDateTime = LocalDateTime.parse(raw, HEVY_DATE_FORMATTER_ES_2);
        }
      }
    }
    Workout w = new Workout();
    w.setUser(user);
    w.setName(record.get("title"));
    w.setStartTime(localDateTime.atOffset(ZoneOffset.UTC));
    w.setNotes(record.get("description"));
    return w;
  }

  private BigDecimal parseBigDecimal(String val) {
    if (val == null) return BigDecimal.ZERO;
    String s = val.trim();
    if (s.isEmpty()) return BigDecimal.ZERO;
    // Hevy / CSV a veces usa coma decimal (ej: 52,5)
    // Si trae ambos separadores, asumimos '.' miles y ',' decimal.
    if (s.contains(",") && s.contains(".")) {
      s = s.replace(".", "");
    }
    s = s.replace(',', '.');
    return new BigDecimal(s);
  }

  private Integer parseInteger(String val) {
    if (val == null) return 0;
    String s = val.trim();
    if (s.isEmpty()) return 0;
    s = s.replace(",", "");
    return Integer.parseInt(s);
  }
}
