package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.ImportResultDTO;
import com.eze.gymanalytics.api.model.*;
import com.eze.gymanalytics.api.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de importación optimizado para grandes CSVs de Hevy.
 *
 * Estrategia de rendimiento:
 *   - ANTES: N individual saves a Supabase (cloud EU) → ~30 ms × 5000+ filas = timeout
 *   - AHORA: 4 round-trips totales con multi-value INSERT ... RETURNING id via JdbcTemplate
 *
 * Flujo:
 *   1. Parseo completo del CSV en memoria (0 queries)
 *   2. INSERT workouts (1 round-trip) → IDs
 *   3. INSERT workout_exercises (1 round-trip) → IDs
 *   4. INSERT series en chunks de 500 (~⌈N/500⌉ round-trips)
 *   5. UPDATE volúmenes de workouts (1 round-trip)
 */
@Service
public class ImportService {

  private static final int SERIE_CHUNK_SIZE = 500;

  private static final Map<String, String> EXERCISE_MUSCLE_MAP = new LinkedHashMap<>();

  static {
    // ── Piernas (específicos antes del catch-all "curl") ──────────────────
    for (String kw : new String[]{
        "leg curl", "leg extension", "split squat", "romanian deadlift",
        "peso muerto rumano", "sentadilla b\u00falgara", "sentadilla bulgara",
        "nordic", "hip hinge", "step up", "prensa", "curl femoral",
        "extensi\u00f3n cu\u00e1dr", "extension cuadr", "cu\u00e1driceps", "cuadriceps",
        "elevaci\u00f3n gemelos", "elevacion gemelos",
        "press de piernas", "extensi\u00f3n de pierna", "extension de pierna",
        "aducc", "abducc",
        "squat", "sentadilla", "leg press", "lunge", "zancada",
        "calf raise", "rdl", "abductores", "adductores"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Piernas");
    }
    // ── Glúteos ───────────────────────────────────────────────────────────
    for (String kw : new String[]{"hip thrust", "glute bridge", "glute kick", "empuje de caderas"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Gl\u00fateos");
    }
    // ── Hombros (overhead press/upright row antes de los catch-alls) ──────
    for (String kw : new String[]{
        "overhead press", "shoulder press", "press militar", "upright row",
        "lateral raise", "front raise", "face pull", "arnold press",
        "rear delt", "deltoides posterior", "vuelos posteriores", "posterior",
        "shrug", "encogimientos", "elevaci\u00f3n lateral", "elevacion lateral",
        "vuelos laterales", "vuelo frontal", "press de hombros", "press arnold"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Hombros");
    }
    // ── Pecho (bench press / fondos pecho antes de los catch-alls) ────────
    for (String kw : new String[]{
        "bench press", "dumbbell press", "incline bench", "incline press",
        "decline press", "pec deck", "crossover", "chest dip", "fondos pecho",
        "aperturas", "cruces", "cruzados", "press inclinado", "press declinado",
        "banca inclinado", "press de banca", "press con mancuernas", "press banca",
        "chest fly", "cable fly", "dumbbell fly", "chest press", "push up", "flexiones"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Pecho");
    }
    // ── Tríceps (antes del catch-all "fondos") ────────────────────────────
    for (String kw : new String[]{
        "skull crusher", "tricep pushdown", "close grip bench", "overhead tricep",
        "pushdown", "extensi\u00f3n de tr\u00edceps", "extension tricep",
        "press franc\u00e9s", "press frances", "fondos tr\u00edceps", "fondos triceps",
        "tricep", "tr\u00edceps", "dips", "fondos"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Tr\u00edceps");
    }
    // ── Espalda (específicos antes del catch-all "row" / "remo") ──────────
    for (String kw : new String[]{
        "lat pulldown", "pulldown", "pull-down",
        "bent over row", "barbell row", "dumbbell row", "cable row",
        "seated row", "t-bar row", "remo sentado", "remo con barra",
        "back extension", "extensi\u00f3n de espalda", "extension de espalda",
        "hyperextension", "good morning", "pull up", "dominadas", "dominada",
        "chin up", "deadlift", "peso muerto", "chin", "dorsal", "polea",
        "jal\u00f3n", "jalon", "row", "remo"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Espalda");
    }
    // ── Bíceps (específicos antes del catch-all "curl") ──────────────────
    for (String kw : new String[]{
        "bicep curl", "curl b\u00edceps", "curl biceps",
        "hammer curl", "preacher curl", "incline curl", "concentration curl",
        "barbell curl", "cable curl", "curl martillo", "curl inclinado",
        "curl concentrado", "b\u00edceps", "biceps", "curl"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "B\u00edceps");
    }
    // ── Core ─────────────────────────────────────────────────────────────
    for (String kw : new String[]{
        "ab wheel", "rueda abdominal", "cable crunch", "hanging leg raise",
        "russian twist", "mountain climber", "sit up",
        "elevaci\u00f3n de piernas", "elevacion de piernas",
        "plank", "plancha", "crunch", "abdominales", "hollow", "tijeras"}) {
      EXERCISE_MUSCLE_MAP.put(kw, "Core");
    }
  }

  private final ExerciseRepository exerciseRepository;
  private final ProfileRepository profileRepository;
  private final JdbcTemplate jdbcTemplate;

  private static LocalDateTime parseHevyDate(String str) {
    DateTimeFormatter[] FORMATS = {
        DateTimeFormatter.ofPattern("MMM d yyyy, HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", new Locale("es")),
    };
    for (DateTimeFormatter fmt : FORMATS) {
      try { return LocalDateTime.parse(str.trim(), fmt); } catch (Exception ignored) {}
    }
    throw new RuntimeException("Fecha no reconocida: " + str);
  }

  public ImportService(ExerciseRepository exerciseRepository,
                       ProfileRepository profileRepository,
                       JdbcTemplate jdbcTemplate) {
    this.exerciseRepository = exerciseRepository;
    this.profileRepository = profileRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  // ── Estructuras en memoria (static para poder usarlas en lambdas) ──────────

  private static final class WorkoutHolder {
    final UUID userId;
    final String name;
    final Timestamp startTime;
    final Timestamp endTime;
    final String notes;

    WorkoutHolder(UUID userId, String name, Timestamp startTime, Timestamp endTime, String notes) {
      this.userId = userId;
      this.name = name;
      this.startTime = startTime;
      this.endTime = endTime;
      this.notes = notes;
    }
  }

  private static final class WEHolder {
    final String workoutKey;
    final Long exerciseId;
    final int exerciseOrder;
    final String notes;

    WEHolder(String workoutKey, Long exerciseId, int exerciseOrder, String notes) {
      this.workoutKey = workoutKey;
      this.exerciseId = exerciseId;
      this.exerciseOrder = exerciseOrder;
      this.notes = notes;
    }
  }

  private static final class SerieHolder {
    final String weKey;
    final int setOrder;
    final BigDecimal weight;
    final int reps;
    final BigDecimal rpe;      // nullable
    final boolean isWarmup;

    SerieHolder(String weKey, int setOrder, BigDecimal weight, int reps,
                BigDecimal rpe, boolean isWarmup) {
      this.weKey = weKey;
      this.setOrder = setOrder;
      this.weight = weight;
      this.reps = reps;
      this.rpe = rpe;
      this.isWarmup = isWarmup;
    }
  }

  // ── Patch de duraciones para workouts existentes ────────────────────────────

  @Transactional
  public int patchEndTimes(MultipartFile file, String userEmail) {
    Optional<Profile> userOpt = profileRepository.findByEmail(userEmail);
    if (userOpt.isEmpty()) return 0;
    UUID userId = userOpt.get().getId();

    // Parse CSV extracting only start_time → end_time mapping (first occurrence wins)
    Map<Timestamp, Timestamp> startToEnd = new LinkedHashMap<>();

    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser csv = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())
    ) {
      for (CSVRecord row : csv) {
        String startStr = row.get("start_time");
        String endStr   = row.get("end_time");
        if (startStr == null || startStr.isBlank() || endStr == null || endStr.isBlank()) continue;
        try {
          Timestamp startTs = Timestamp.from(
              parseHevyDate(startStr).toInstant(ZoneOffset.UTC));
          if (startToEnd.containsKey(startTs)) continue;
          Timestamp endTs = Timestamp.from(
              parseHevyDate(endStr).toInstant(ZoneOffset.UTC));
          startToEnd.put(startTs, endTs);
        } catch (Exception ignored) {}
      }
    } catch (Exception e) {
      throw new RuntimeException("Error al leer el CSV: " + e.getMessage());
    }

    int updated = 0;
    for (Map.Entry<Timestamp, Timestamp> entry : startToEnd.entrySet()) {
      updated += jdbcTemplate.update(
          "UPDATE workouts SET end_time = ? WHERE user_id = ? AND start_time = ? AND end_time IS NULL",
          entry.getValue(), userId, entry.getKey()
      );
    }
    return updated;
  }

  // ── Método principal ────────────────────────────────────────────────────────

  @Transactional
  public ImportResultDTO importHevyCsv(MultipartFile file, String userEmail) {

    // 1. Resolver perfil (1 query)
    Profile user = profileRepository.findByEmail(userEmail).orElseGet(() -> {
      Profile p = new Profile();
      p.setId(UUID.randomUUID());
      p.setEmail(userEmail);
      p.setUsername(userEmail.split("@")[0]);
      p.setRole("user");
      return profileRepository.save(p);
    });

    // 2. Cargar todos los ejercicios existentes en caché (1 query)
    Map<String, Long> exerciseCache = new HashMap<>();
    exerciseRepository.findAll().forEach(ex ->
        exerciseCache.put(ex.getName().toLowerCase(), ex.getId())
    );

    // 3. Parseo completo del CSV en estructuras en memoria (0 queries a DB)
    LinkedHashMap<String, WorkoutHolder> workoutMap = new LinkedHashMap<>();
    LinkedHashMap<String, WEHolder> weMap = new LinkedHashMap<>();
    List<SerieHolder> seriesData = new ArrayList<>();
    Map<String, BigDecimal> workoutVolumes = new HashMap<>();
    Map<String, Integer> weOrderCounters = new HashMap<>();
    List<String> failedRows = new ArrayList<>();
    int successCount = 0;

    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser csv = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())
    ) {
      for (CSVRecord row : csv) {
        try {
          final String startTimeStr  = row.get("start_time");
          final String endTimeStr    = row.get("end_time");
          final String exerciseName  = row.get("exercise_title");
          final String title         = row.get("title");
          final String description   = row.get("description");
          final String exerciseNotes = row.get("exercise_notes");

          workoutMap.computeIfAbsent(startTimeStr, k -> {
            LocalDateTime ldt = parseHevyDate(startTimeStr);
            Timestamp endTs = null;
            if (endTimeStr != null && !endTimeStr.isBlank()) {
              try {
                LocalDateTime ldtEnd = parseHevyDate(endTimeStr);
                endTs = Timestamp.from(ldtEnd.toInstant(ZoneOffset.UTC));
              } catch (Exception ignored) {}
            }
            workoutVolumes.put(startTimeStr, BigDecimal.ZERO);
            return new WorkoutHolder(
                user.getId(), title,
                Timestamp.from(ldt.toInstant(ZoneOffset.UTC)),
                endTs,
                description);
          });

          // Ejercicio: caché → nueva entrada solo si no existe (rara vez)
          Long exerciseId = exerciseCache.computeIfAbsent(exerciseName.toLowerCase(), k -> {
            String muscleGroup = detectMuscleGroup(exerciseName);
            Exercise ex = new Exercise();
            ex.setName(exerciseName);
            ex.setMuscleGroup(muscleGroup);
            ex.setDescription("Creado automáticamente");
            return exerciseRepository.save(ex).getId();
          });

          String weKey = startTimeStr + "|" + exerciseName;
          weMap.computeIfAbsent(weKey, k -> {
            int order = weOrderCounters.merge(startTimeStr, 1, Integer::sum);
            return new WEHolder(startTimeStr, exerciseId, order, exerciseNotes);
          });

          BigDecimal weight = parseBigDecimal(row.get("weight_kg"));
          int reps = parseInteger(row.get("reps"));
          workoutVolumes.merge(startTimeStr, weight.multiply(BigDecimal.valueOf(reps)), BigDecimal::add);

          seriesData.add(new SerieHolder(
              weKey,
              Integer.parseInt(row.get("set_index")),
              weight,
              reps,
              parseNullableBigDecimal(row.get("rpe")),
              row.get("set_type").equalsIgnoreCase("warmup")
          ));
          successCount++;

        } catch (Exception e) {
          failedRows.add("Fila " + row.getRecordNumber() + ": " + e.getMessage());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error al leer el CSV: " + e.getMessage());
    }

    if (workoutMap.isEmpty()) {
      return new ImportResultDTO(0, failedRows.size(), failedRows);
    }

    // 4. INSERT workouts → IDs (1 round-trip a cloud DB)
    List<String> workoutKeys = new ArrayList<>(workoutMap.keySet());
    List<Long> workoutIds = batchInsertWorkoutsReturningIds(workoutMap, workoutKeys);
    Map<String, Long> workoutKeyToId = new HashMap<>();
    for (int i = 0; i < workoutKeys.size(); i++) {
      workoutKeyToId.put(workoutKeys.get(i), workoutIds.get(i));
    }

    // 5. INSERT workout_exercises → IDs (1 round-trip)
    List<String> weKeys = new ArrayList<>(weMap.keySet());
    List<Long> weIds = batchInsertWorkoutExercisesReturningIds(weMap, weKeys, workoutKeyToId);
    Map<String, Long> weKeyToId = new HashMap<>();
    for (int i = 0; i < weKeys.size(); i++) {
      weKeyToId.put(weKeys.get(i), weIds.get(i));
    }

    // 6. INSERT series en chunks de 500 (~⌈N/500⌉ round-trips)
    batchInsertSeries(seriesData, weKeyToId);

    // 7. UPDATE total_volume de cada workout (1 round-trip)
    batchUpdateWorkoutVolumes(workoutKeyToId, workoutVolumes);

    return new ImportResultDTO(successCount, failedRows.size(), failedRows);
  }

  // ── Helpers de batch insert ─────────────────────────────────────────────────

  private List<Long> batchInsertWorkoutsReturningIds(
      Map<String, WorkoutHolder> workoutMap, List<String> keys) {

    StringBuilder sql = new StringBuilder(
        "INSERT INTO workouts (user_id, name, start_time, end_time, notes, total_volume, created_at) VALUES ");
    List<Object> params = new ArrayList<>();

    for (int i = 0; i < keys.size(); i++) {
      sql.append("(?,?,?,?,?,0,NOW())");
      if (i < keys.size() - 1) sql.append(",");
      WorkoutHolder wh = workoutMap.get(keys.get(i));
      params.add(wh.userId);
      params.add(wh.name);
      params.add(wh.startTime);
      params.add(wh.endTime);
      params.add(wh.notes);
    }
    sql.append(" RETURNING id");
    return jdbcTemplate.queryForList(sql.toString(), Long.class, params.toArray());
  }

  private List<Long> batchInsertWorkoutExercisesReturningIds(
      Map<String, WEHolder> weMap, List<String> keys, Map<String, Long> workoutKeyToId) {

    StringBuilder sql = new StringBuilder(
        "INSERT INTO workout_exercises (workout_id, exercise_id, exercise_order, notes) VALUES ");
    List<Object> params = new ArrayList<>();

    for (int i = 0; i < keys.size(); i++) {
      sql.append("(?,?,?,?)");
      if (i < keys.size() - 1) sql.append(",");
      WEHolder wed = weMap.get(keys.get(i));
      params.add(workoutKeyToId.get(wed.workoutKey));
      params.add(wed.exerciseId);
      params.add(wed.exerciseOrder);
      params.add(wed.notes);
    }
    sql.append(" RETURNING id");
    return jdbcTemplate.queryForList(sql.toString(), Long.class, params.toArray());
  }

  private void batchInsertSeries(List<SerieHolder> series, Map<String, Long> weKeyToId) {
    for (int start = 0; start < series.size(); start += SERIE_CHUNK_SIZE) {
      List<SerieHolder> chunk = series.subList(start, Math.min(start + SERIE_CHUNK_SIZE, series.size()));

      StringBuilder sql = new StringBuilder(
          "INSERT INTO series (workout_exercise_id, set_order, weight, reps, rpe, is_warmup) VALUES ");
      List<Object> params = new ArrayList<>();

      for (int i = 0; i < chunk.size(); i++) {
        sql.append("(?,?,?,?,?,?)");
        if (i < chunk.size() - 1) sql.append(",");
        SerieHolder sd = chunk.get(i);
        params.add(weKeyToId.get(sd.weKey));
        params.add(sd.setOrder);
        params.add(sd.weight);
        params.add(sd.reps);
        params.add(sd.rpe);       // null si no se registró RPE
        params.add(sd.isWarmup);
      }
      jdbcTemplate.update(sql.toString(), params.toArray());
    }
  }

  private void batchUpdateWorkoutVolumes(
      Map<String, Long> workoutKeyToId, Map<String, BigDecimal> workoutVolumes) {

    List<Object[]> batchParams = workoutKeyToId.entrySet().stream()
        .map(e -> new Object[]{workoutVolumes.getOrDefault(e.getKey(), BigDecimal.ZERO), e.getValue()})
        .collect(Collectors.toList());
    jdbcTemplate.batchUpdate("UPDATE workouts SET total_volume = ? WHERE id = ?", batchParams);
  }

  // ── Parsers ─────────────────────────────────────────────────────────────────

  private BigDecimal parseBigDecimal(String val) {
    return (val == null || val.isEmpty()) ? BigDecimal.ZERO : new BigDecimal(val);
  }

  private BigDecimal parseNullableBigDecimal(String val) {
    return (val == null || val.isEmpty()) ? null : new BigDecimal(val);
  }

  private Integer parseInteger(String val) {
    return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val);
  }

  private String detectMuscleGroup(String exerciseName) {
    if (exerciseName == null) return "Otros";
    String lower = exerciseName.toLowerCase();
    for (Map.Entry<String, String> entry : EXERCISE_MUSCLE_MAP.entrySet()) {
      if (lower.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return "Otros";
  }
}
