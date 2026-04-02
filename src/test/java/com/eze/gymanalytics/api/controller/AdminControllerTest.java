package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for AdminController.
 *
 * Covers EZE-186 scenarios: 200 for admin, 403 for non-admin, 401 for no auth.
 *
 * Uses an isolated test SecurityConfig that applies the same authorization
 * rules (/api/admin/** requires authentication) without loading the full
 * JwtAuthenticationFilter bean (which requires external Supabase JWKS).
 */
@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.TestSecurityConfig.class)
class AdminControllerTest {

    /**
     * Minimal security config for the test slice.
     * Mirrors the production rules for /api/admin/** without the JWT filter.
     */
    @Configuration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/admin/**").authenticated()
                    .anyRequest().permitAll()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ProfileRepository profileRepository;

    // ──────────────────────────────────────────────────────────
    // GET /api/admin/stats
    // ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@example.com")
    void getStats_asAdmin_returns200WithPayload() throws Exception {
        Profile adminProfile = buildProfile("admin@example.com", "admin");
        when(profileRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminProfile));
        when(adminService.getStats()).thenReturn(new AdminStatsDTO(24, 1847, 312, 8));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(24))
                .andExpect(jsonPath("$.totalWorkouts").value(1847))
                .andExpect(jsonPath("$.totalExercises").value(312))
                .andExpect(jsonPath("$.activeLastWeek").value(8));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getStats_asNonAdmin_returns403() throws Exception {
        Profile userProfile = buildProfile("user@example.com", "user");
        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userProfile));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStats_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────
    // GET /api/admin/users
    // ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@example.com")
    void getUsers_asAdmin_returns200WithList() throws Exception {
        Profile adminProfile = buildProfile("admin@example.com", "admin");
        when(profileRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminProfile));

        UserProfileDTO dto = new UserProfileDTO(
                UUID.randomUUID(), "user@example.com", "user1", "user", "2025-01-01T00:00:00Z");
        when(adminService.getUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@example.com"))
                .andExpect(jsonPath("$[0].role").value("user"));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void getUsers_asAdmin_whenNoPlatformUsers_returns200WithEmptyArray() throws Exception {
        Profile adminProfile = buildProfile("admin@example.com", "admin");
        when(profileRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminProfile));
        when(adminService.getUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getUsers_asNonAdmin_returns403() throws Exception {
        Profile userProfile = buildProfile("user@example.com", "user");
        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userProfile));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private Profile buildProfile(String email, String role) {
        Profile p = new Profile();
        p.setId(UUID.randomUUID());
        p.setEmail(email);
        p.setUsername(email.split("@")[0]);
        p.setRole(role);
        return p;
    }
}
