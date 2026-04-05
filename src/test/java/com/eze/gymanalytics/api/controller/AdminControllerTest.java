package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de slice para AdminController.
 *
 * Se usa una SecurityConfig de test que:
 *  - No incluye el filtro JWT (no se conecta a Supabase JWKS)
 *  - Requiere autenticación en /api/** (para probar 401)
 *  - Configura el entrypoint para devolver 401 (no 403) en llamadas no autenticadas
 *
 * Se mockean:
 *  - AdminService: para controlar las respuestas del servicio
 *  - ProfileRepository: para controlar la verificación del rol en el controlador
 */
@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.TestSecurityConfig.class)
class AdminControllerTest {

    /**
     * Configuración de seguridad para tests: stateless, sin filtro JWT,
     * con AuthenticationEntryPoint que devuelve 401.
     */
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
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

    // ─────────────────────────────────────────────────────
    // GET /api/admin/stats
    // ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@gym.com")
    void getStats_withAdminRole_returns200() throws Exception {
        Profile adminProfile = buildProfile("admin@gym.com", "admin");
        when(profileRepository.findByEmail("admin@gym.com")).thenReturn(Optional.of(adminProfile));
        when(adminService.getStats()).thenReturn(new AdminStatsDTO(10, 50, 100, 5));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalWorkouts").value(50))
                .andExpect(jsonPath("$.totalExercises").value(100))
                .andExpect(jsonPath("$.activeLastWeek").value(5));
    }

    @Test
    @WithMockUser(username = "user@gym.com")
    void getStats_withNonAdminRole_returns403() throws Exception {
        Profile regularProfile = buildProfile("user@gym.com", "user");
        when(profileRepository.findByEmail("user@gym.com")).thenReturn(Optional.of(regularProfile));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStats_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────
    // GET /api/admin/users
    // ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@gym.com")
    void getUsers_withAdminRole_returns200() throws Exception {
        Profile adminProfile = buildProfile("admin@gym.com", "admin");
        when(profileRepository.findByEmail("admin@gym.com")).thenReturn(Optional.of(adminProfile));

        UserProfileDTO userDTO = new UserProfileDTO(UUID.randomUUID(), "user@gym.com", "user1", "user", null);
        when(adminService.getUsers()).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@gym.com"));
    }

    @Test
    @WithMockUser(username = "user@gym.com")
    void getUsers_withNonAdminRole_returns403() throws Exception {
        Profile regularProfile = buildProfile("user@gym.com", "user");
        when(profileRepository.findByEmail("user@gym.com")).thenReturn(Optional.of(regularProfile));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────
    // Escenario: rol revocado entre solicitudes (EZE-193)
    // ─────────────────────────────────────────────────────

    /**
     * Verifica que si el rol del usuario es revocado en la DB entre dos solicitudes,
     * el controlador refleja el nuevo estado en la siguiente petición (siempre consulta DB).
     *
     * Flujo:
     *  1. Primera petición → usuario tiene rol "admin" → 200
     *  2. El rol es revocado en la DB → ahora tiene rol "user"
     *  3. Segunda petición → controlador re-consulta DB → 403
     */
    @Test
    @WithMockUser(username = "admin@gym.com")
    void getStats_roleRevokedBetweenRequests_returns403OnNextRequest() throws Exception {
        // Primera solicitud: es admin → 200
        Profile adminProfile = buildProfile("admin@gym.com", "admin");
        when(profileRepository.findByEmail("admin@gym.com")).thenReturn(Optional.of(adminProfile));
        when(adminService.getStats()).thenReturn(new AdminStatsDTO(1, 1, 1, 1));

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk());

        // Se revoca el rol: ahora es "user" en la DB
        Profile revokedProfile = buildProfile("admin@gym.com", "user");
        when(profileRepository.findByEmail("admin@gym.com")).thenReturn(Optional.of(revokedProfile));

        // Segunda solicitud: el controlador vuelve a consultar la DB → 403
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────

    private Profile buildProfile(String email, String role) {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setEmail(email);
        profile.setUsername(email.split("@")[0]);
        profile.setRole(role);
        return profile;
    }
}
