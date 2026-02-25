package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.WorkoutDTO;
import com.eze.gymanalytics.api.service.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<WorkoutDTO> createWorkout(@RequestBody WorkoutDTO workoutDTO,
                                                    @RequestParam("email") String email) {
        try {
            WorkoutDTO createdWorkout = workoutService.createWorkout(workoutDTO, email);
            return ResponseEntity.ok(createdWorkout);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<?> getWorkouts(
            @RequestParam("email") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<WorkoutDTO> workouts = workoutService.getUserWorkouts(email, pageable, startDate, endDate);
            return ResponseEntity.ok(workouts);
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error exacto en la terminal del server
            return ResponseEntity.badRequest().body("Error al obtener workouts: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkout(@PathVariable Long id, @RequestParam("email") String email) {
        try {
            workoutService.deleteWorkout(id, email);
            return ResponseEntity.ok("Entrenamiento borrado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
