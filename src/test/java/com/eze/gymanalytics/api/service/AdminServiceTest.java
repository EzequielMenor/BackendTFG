package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }

    // ─────────────────────────────────────────────────────
    // getStats()
    // ─────────────────────────────────────────────────────

    @Test
    void getStats_returnsAggregatedCounts() {
        when(profileRepository.count()).thenReturn(5L);
        when(workoutRepository.count()).thenReturn(20L);
        when(exerciseRepository.count()).thenReturn(50L);
        when(workoutRepository.countDistinctActiveUsersSince(any(OffsetDateTime.class))).thenReturn(3L);

        AdminStatsDTO stats = adminService.getStats();

        assertThat(stats.getTotalUsers()).isEqualTo(5L);
        assertThat(stats.getTotalWorkouts()).isEqualTo(20L);
        assertThat(stats.getTotalExercises()).isEqualTo(50L);
        assertThat(stats.getActiveLastWeek()).isEqualTo(3L);
    }

    @Test
    void getStats_withEmptyPlatform_returnsAllZeros() {
        when(profileRepository.count()).thenReturn(0L);
        when(workoutRepository.count()).thenReturn(0L);
        when(exerciseRepository.count()).thenReturn(0L);
        when(workoutRepository.countDistinctActiveUsersSince(any(OffsetDateTime.class))).thenReturn(0L);

        AdminStatsDTO stats = adminService.getStats();

        assertThat(stats.getTotalUsers()).isZero();
        assertThat(stats.getTotalWorkouts()).isZero();
        assertThat(stats.getTotalExercises()).isZero();
        assertThat(stats.getActiveLastWeek()).isZero();
    }

    // ─────────────────────────────────────────────────────
    // getUsers()
    // ─────────────────────────────────────────────────────

    @Test
    void getUsers_mapsProfilesToDTOs() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setEmail("test@example.com");
        profile.setUsername("testuser");
        profile.setRole("user");
        profile.setCreatedAt(OffsetDateTime.now());

        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<UserProfileDTO> users = adminService.getUsers();

        assertThat(users).hasSize(1);
        UserProfileDTO dto = users.get(0);
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getRole()).isEqualTo("user");
    }

    @Test
    void getUsers_withEmptyPlatform_returnsEmptyList() {
        when(profileRepository.findAll()).thenReturn(List.of());

        List<UserProfileDTO> users = adminService.getUsers();

        assertThat(users).isEmpty();
    }
}
