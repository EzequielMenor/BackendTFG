package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProfileRepository profileRepository;

    public AdminController(AdminService adminService, ProfileRepository profileRepository) {
        this.adminService = adminService;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal String email) {
        try {
            Profile profile = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            if (!"admin".equals(profile.getRole())) {
                return ResponseEntity.status(403).body("Sin permisos de administrador");
            }
            AdminStatsDTO stats = adminService.getStats();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Sin permisos de administrador") ||
                    e.getMessage().equals("Usuario no encontrado")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal String email) {
        try {
            Profile profile = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            if (!"admin".equals(profile.getRole())) {
                return ResponseEntity.status(403).body("Sin permisos de administrador");
            }
            List<UserProfileDTO> users = adminService.getUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Sin permisos de administrador") ||
                    e.getMessage().equals("Usuario no encontrado")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
