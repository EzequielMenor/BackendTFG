package com.eze.gymanalytics.api.controller;

import com.eze.gymanalytics.api.dto.analytics.AnalyticsSummaryDTO;
import com.eze.gymanalytics.api.service.AnalyticsService;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de slice para AnalyticsController — endpoint GET /api/v1/analytics/summary.
 *
 * Se usa una SecurityConfig de test que:
 *  - No incluye el filtro JWT (no se conecta a Supabase JWKS)
 *  - Requiere autenticación en /api/** (para probar 401)
 *  - Configura el entrypoint para devolver 401 (no 403) en llamadas no autenticadas
 */
@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsControllerTest.TestSecurityConfig.class)
class AnalyticsControllerTest {

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
    private AnalyticsService analyticsService;

    // ─────────────────────────────────────────────────────
    // GET /api/v1/analytics/summary
    // ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@gym.com")
    void getSummary_withValidParams_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        AnalyticsSummaryDTO dto = new AnalyticsSummaryDTO(
                12L,
                new BigDecimal("4500.50"),
                "Chest",
                58.3
        );

        when(analyticsService.getSummary(eq(userId), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/summary")
                        .param("userId", userId.toString())
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkouts").value(12))
                .andExpect(jsonPath("$.totalVolume").value(4500.50))
                .andExpect(jsonPath("$.topMuscleGroup").value("Chest"))
                .andExpect(jsonPath("$.avgDurationMinutes").value(58.3));
    }

    @Test
    @WithMockUser(username = "user@gym.com")
    void getSummary_withMalformedFrom_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/summary")
                        .param("userId", UUID.randomUUID().toString())
                        .param("from", "not-a-date")
                        .param("to", "2025-12-31T23:59:59Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@gym.com")
    void getSummary_missingUserId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/summary")
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-12-31T23:59:59Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/summary")
                        .param("userId", UUID.randomUUID().toString())
                        .param("from", "2025-01-01T00:00:00Z")
                        .param("to", "2025-12-31T23:59:59Z"))
                .andExpect(status().isUnauthorized());
    }
}
