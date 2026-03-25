package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.RoutineDTO;
import com.eze.gymanalytics.api.service.RoutineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @GetMapping
    public ResponseEntity<?> getUserRoutines(@AuthenticationPrincipal String email) {
        try {
            List<RoutineDTO> routines = routineService.getUserRoutines(email);
            return ResponseEntity.ok(routines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener rutinas: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRoutine(@RequestBody RoutineDTO routineDTO,
                                           @AuthenticationPrincipal String email) {
        try {
            RoutineDTO created = routineService.createRoutine(routineDTO, email);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear rutina: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoutine(@PathVariable Long id,
                                                @AuthenticationPrincipal String email) {
        try {
            routineService.deleteRoutine(id, email);
            return ResponseEntity.ok("Rutina borrada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
