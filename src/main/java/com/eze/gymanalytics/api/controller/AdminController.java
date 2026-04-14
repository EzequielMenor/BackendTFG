package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.AdminExerciseDTO;
import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.AdminWorkoutDTO;
import com.eze.gymanalytics.api.dto.AdminWorkoutDetailDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProfileRepository profileRepository;

    public AdminController(AdminService adminService, ProfileRepository profileRepository) {
        this.adminService = adminService;
        this.profileRepository = profileRepository;
    }

    /**
     * Devuelve estadísticas globales de la plataforma.
     * Solo accesible para usuarios con role = "admin" (verificado en DB, no en JWT).
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminStatsDTO stats = adminService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Devuelve la lista completa de usuarios registrados en la plataforma.
     * Solo accesible para usuarios con role = "admin" (verificado en DB, no en JWT).
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        List<UserProfileDTO> users = adminService.getUsers();
        return ResponseEntity.ok(users);
    }

    // -------------------------------------------------------------------------
    // Workouts CRUD
    // -------------------------------------------------------------------------

    /**
     * Devuelve todos los entrenamientos de la plataforma (vista admin).
     * GET /api/admin/workouts
     */
    @GetMapping("/workouts")
    public ResponseEntity<?> getAllWorkouts() {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        List<AdminWorkoutDTO> workouts = adminService.getAllWorkouts();
        return ResponseEntity.ok(workouts);
    }

    /**
     * Devuelve el detalle de un entrenamiento por id (incluye ejercicios).
     * GET /api/admin/workouts/{id}
     */
    @GetMapping("/workouts/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Long id) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminWorkoutDetailDTO detail = adminService.getWorkoutById(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * Actualiza los metadatos editables de un entrenamiento (name, notes).
     * PUT /api/admin/workouts/{id}
     * Body: { "name": "...", "notes": "..." }
     */
    @PutMapping("/workouts/{id}")
    public ResponseEntity<?> updateWorkout(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminWorkoutDTO updated = adminService.updateWorkout(id, body.get("name"), body.get("notes"));
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un entrenamiento por id.
     * DELETE /api/admin/workouts/{id}
     */
    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        adminService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Exercises CRUD
    // -------------------------------------------------------------------------

    /**
     * Devuelve todos los ejercicios del catálogo.
     * GET /api/admin/exercises
     */
    @GetMapping("/exercises")
    public ResponseEntity<?> getAllExercises() {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        List<AdminExerciseDTO> exercises = adminService.getAllExercises();
        return ResponseEntity.ok(exercises);
    }

    /**
     * Devuelve el detalle de un ejercicio por id.
     * GET /api/admin/exercises/{id}
     */
    @GetMapping("/exercises/{id}")
    public ResponseEntity<?> getExerciseById(@PathVariable Long id) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminExerciseDTO exercise = adminService.getExerciseById(id);
        return ResponseEntity.ok(exercise);
    }

    /**
     * Crea un nuevo ejercicio en el catálogo.
     * POST /api/admin/exercises
     * Body: { "name": "...", "muscleGroup": "...", ... }
     * Devuelve 201 Created con el ejercicio creado.
     */
    @PostMapping("/exercises")
    public ResponseEntity<?> createExercise(@RequestBody Map<String, Object> body) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminExerciseDTO created = adminService.createExercise(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un ejercicio existente.
     * PUT /api/admin/exercises/{id}
     * Body: campos a actualizar (todos opcionales excepto name si se envía)
     */
    @PutMapping("/exercises/{id}")
    public ResponseEntity<?> updateExercise(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        AdminExerciseDTO updated = adminService.updateExercise(id, body);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un ejercicio del catálogo.
     * DELETE /api/admin/exercises/{id}
     */
    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
        String email = getAuthenticatedEmail();
        if (!isAdmin(email)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol admin");
        }
        adminService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Extrae el nombre del principal del contexto de seguridad de Spring.
     * En producción: email extraído del JWT por JwtAuthenticationFilter.
     * En tests con @WithMockUser: username del mock.
     */
    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Comprueba en la base de datos si el usuario autenticado tiene rol "admin".
     * Este enfoque permite detectar revocaciones de rol entre solicitudes.
     */
    private boolean isAdmin(String email) {
        if (email == null) return false;
        return profileRepository.findByEmail(email)
                .map(profile -> "admin".equals(profile.getRole()))
                .orElse(false);
    }
}

