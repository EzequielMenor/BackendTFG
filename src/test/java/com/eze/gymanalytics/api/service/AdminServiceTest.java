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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminService.
 *
 * Covers EZE-186: getStats() count aggregation and getUsers() profile mapping.
 */
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

    // ──────────────────────────────────────────────────────────
    // getStats()
    // ──────────────────────────────────────────────────────────

    @Test
    void getStats_returnsAggregatedCounts() {
        when(profileRepository.count()).thenReturn(24L);
        when(workoutRepository.count()).thenReturn(1847L);
        when(exerciseRepository.count()).thenReturn(312L);
        when(workoutRepository.countDistinctActiveUsersSince(any(OffsetDateTime.class))).thenReturn(8L);

        AdminStatsDTO stats = adminService.getStats();

        assertThat(stats.getTotalUsers()).isEqualTo(24L);
        assertThat(stats.getTotalWorkouts()).isEqualTo(1847L);
        assertThat(stats.getTotalExercises()).isEqualTo(312L);
        assertThat(stats.getActiveLastWeek()).isEqualTo(8L);
    }

    @Test
    void getStats_whenNoData_returnsZeroCounts() {
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

    // ──────────────────────────────────────────────────────────
    // getUsers()
    // ──────────────────────────────────────────────────────────

    @Test
    void getUsers_mapsProfileFieldsCorrectly() {
        Profile profile = new Profile();
        UUID id = UUID.randomUUID();
        profile.setId(id);
        profile.setEmail("admin@example.com");
        profile.setUsername("adminUser");
        profile.setRole("admin");
        profile.setCreatedAt(OffsetDateTime.parse("2025-01-15T10:00:00Z"));

        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<UserProfileDTO> users = adminService.getUsers();

        assertThat(users).hasSize(1);
        UserProfileDTO dto = users.get(0);
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getEmail()).isEqualTo("admin@example.com");
        assertThat(dto.getUsername()).isEqualTo("adminUser");
        assertThat(dto.getRole()).isEqualTo("admin");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void getUsers_whenNoPlatformUsers_returnsEmptyList() {
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserProfileDTO> users = adminService.getUsers();

        assertThat(users).isEmpty();
    }

    @Test
    void getUsers_whenCreatedAtIsNull_setsCreatedAtToNull() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setEmail("user@example.com");
        profile.setUsername("someUser");
        profile.setRole("user");
        // createdAt not set — remains null

        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<UserProfileDTO> users = adminService.getUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getCreatedAt()).isNull();
    }
}
