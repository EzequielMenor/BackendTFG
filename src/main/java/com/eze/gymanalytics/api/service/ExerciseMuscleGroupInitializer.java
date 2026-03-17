package com.eze.gymanalytics.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs once at startup and patches exercises that have muscleGroup='Otros'
 * using the same keyword logic as ImportService.
 */
@Component
public class ExerciseMuscleGroupInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExerciseMuscleGroupInitializer.class);

    private static final Map<String, String> GROUP_KEYWORDS = new LinkedHashMap<>();

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
            GROUP_KEYWORDS.put(kw, "Piernas");
        }
        // ── Glúteos ───────────────────────────────────────────────────────────
        for (String kw : new String[]{"hip thrust", "glute bridge", "glute kick", "empuje de caderas"}) {
            GROUP_KEYWORDS.put(kw, "Gl\u00fateos");
        }
        // ── Hombros (overhead press/upright row antes de los catch-alls) ──────
        for (String kw : new String[]{
                "overhead press", "shoulder press", "press militar", "upright row",
                "lateral raise", "front raise", "face pull", "arnold press",
                "rear delt", "deltoides posterior", "vuelos posteriores", "posterior",
                "shrug", "encogimientos", "elevaci\u00f3n lateral", "elevacion lateral",
                "vuelos laterales", "vuelo frontal", "press de hombros", "press arnold"}) {
            GROUP_KEYWORDS.put(kw, "Hombros");
        }
        // ── Pecho (bench press / fondos pecho antes de los catch-alls) ────────
        for (String kw : new String[]{
                "bench press", "dumbbell press", "incline bench", "incline press",
                "decline press", "pec deck", "crossover", "chest dip", "fondos pecho",
                "aperturas", "cruces", "cruzados", "press inclinado", "press declinado",
                "banca inclinado", "press de banca", "press con mancuernas", "press banca",
                "chest fly", "cable fly", "dumbbell fly", "chest press", "push up", "flexiones"}) {
            GROUP_KEYWORDS.put(kw, "Pecho");
        }
        // ── Tríceps (antes del catch-all "fondos") ────────────────────────────
        for (String kw : new String[]{
                "skull crusher", "tricep pushdown", "close grip bench", "overhead tricep",
                "pushdown", "extensi\u00f3n de tr\u00edceps", "extension tricep",
                "press franc\u00e9s", "press frances", "fondos tr\u00edceps", "fondos triceps",
                "tricep", "tr\u00edceps", "dips", "fondos"}) {
            GROUP_KEYWORDS.put(kw, "Tr\u00edceps");
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
            GROUP_KEYWORDS.put(kw, "Espalda");
        }
        // ── Bíceps (específicos antes del catch-all "curl") ──────────────────
        for (String kw : new String[]{
                "bicep curl", "curl b\u00edceps", "curl biceps",
                "hammer curl", "preacher curl", "incline curl", "concentration curl",
                "barbell curl", "cable curl", "curl martillo", "curl inclinado",
                "curl concentrado", "b\u00edceps", "biceps", "curl"}) {
            GROUP_KEYWORDS.put(kw, "B\u00edceps");
        }
        // ── Core ─────────────────────────────────────────────────────────────
        for (String kw : new String[]{
                "ab wheel", "rueda abdominal", "cable crunch", "hanging leg raise",
                "russian twist", "mountain climber", "sit up",
                "elevaci\u00f3n de piernas", "elevacion de piernas",
                "plank", "plancha", "crunch", "abdominales", "hollow", "tijeras"}) {
            GROUP_KEYWORDS.put(kw, "Core");
        }
    }

    private final JdbcTemplate jdbcTemplate;

    public ExerciseMuscleGroupInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixMuscleGroups() {
        int total = 0;
        for (Map.Entry<String, String> entry : GROUP_KEYWORDS.entrySet()) {
            int updated = jdbcTemplate.update(
                "UPDATE exercises SET muscle_group = ? " +
                "WHERE (muscle_group = 'Otros' OR muscle_group IS NULL) " +
                "AND LOWER(name) LIKE ?",
                entry.getValue(),
                "%" + entry.getKey() + "%"
            );
            total += updated;
        }
        if (total > 0) {
            log.info("ExerciseMuscleGroupInitializer: {} ejercicios actualizados con grupo muscular.", total);
        }
    }
}
