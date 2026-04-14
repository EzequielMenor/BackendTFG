package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.AdminExerciseDTO;
import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Exercise;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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

    // ─────────────────────────────────────────────────────
    // Exercises CRUD
    // ─────────────────────────────────────────────────────

    @Test
    void getAllExercises_returnsMappedDTOsOrderedByName() {
        Exercise e1 = buildExercise(1L, "Sentadilla", "Piernas");
        Exercise e2 = buildExercise(2L, "Auge", "Pecho");
        // findAllByOrderByNameAsc ya devuelve en orden; el test verifica que el servicio mapea correctamente
        when(exerciseRepository.findAllByOrderByNameAsc()).thenReturn(List.of(e2, e1));

        List<AdminExerciseDTO> result = adminService.getAllExercises();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Auge");
        assertThat(result.get(1).getName()).isEqualTo("Sentadilla");
    }

    @Test
    void getExerciseById_existingId_returnsDTO() {
        Exercise exercise = buildExercise(10L, "Press Banca", "Pecho");
        when(exerciseRepository.findById(10L)).thenReturn(Optional.of(exercise));

        AdminExerciseDTO result = adminService.getExerciseById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Press Banca");
        assertThat(result.getMuscleGroup()).isEqualTo("Pecho");
    }

    @Test
    void getExerciseById_nonExistingId_throws404() {
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getExerciseById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createExercise_uniqueName_returnsCreatedDTO() {
        when(exerciseRepository.findByNameIgnoreCase("Curl de biceps")).thenReturn(Optional.empty());
        Exercise saved = buildExercise(5L, "Curl de biceps", "Biceps");
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

        Map<String, Object> body = Map.of("name", "Curl de biceps", "muscleGroup", "Biceps");
        AdminExerciseDTO result = adminService.createExercise(body);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Curl de biceps");
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void createExercise_duplicateName_throws409() {
        Exercise existing = buildExercise(1L, "Press Banca", "Pecho");
        when(exerciseRepository.findByNameIgnoreCase("Press Banca")).thenReturn(Optional.of(existing));

        Map<String, Object> body = Map.of("name", "Press Banca");
        assertThatThrownBy(() -> adminService.createExercise(body))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Press Banca");
    }

    @Test
    void createExercise_missingName_throws400() {
        Map<String, Object> body = Map.of("muscleGroup", "Pecho");

        assertThatThrownBy(() -> adminService.createExercise(body))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nombre es obligatorio");
    }

    @Test
    void updateExercise_existingId_uniqueName_updatesAndReturnsDTO() {
        Exercise exercise = buildExercise(3L, "Fondos", "Triceps");
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.findByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exercise);

        Map<String, Object> body = Map.of("name", "Fondos en paralelas", "muscleGroup", "Triceps");
        AdminExerciseDTO result = adminService.updateExercise(3L, body);

        assertThat(result).isNotNull();
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void updateExercise_nonExistingId_throws404() {
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateExercise(99L, Map.of("name", "Algo")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteExercise_existingId_deletesSuccessfully() {
        when(exerciseRepository.existsById(7L)).thenReturn(true);

        adminService.deleteExercise(7L);

        verify(exerciseRepository).deleteById(7L);
    }

    @Test
    void deleteExercise_nonExistingId_throws404() {
        when(exerciseRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteExercise(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    // ─────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────

    @Test
    void getAllExercises_withNullFields_returnsDTOs() {
        Exercise e1 = new Exercise();
        e1.setId(1L);
        e1.setName("Exercise 1");
        // muscleGroup, aliases, createdAt are null — toAdminExerciseDTO debe manejarlos sin NPE

        when(exerciseRepository.findAllByOrderByNameAsc()).thenReturn(List.of(e1));

        List<AdminExerciseDTO> result = adminService.getAllExercises();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Exercise 1");
        assertThat(result.get(0).getAliases()).isEmpty();
        assertThat(result.get(0).getCreatedAt()).isNull();
    }

    private Exercise buildExercise(Long id, String name, String muscleGroup) {
        Exercise e = new Exercise();
        e.setId(id);
        e.setName(name);
        e.setMuscleGroup(muscleGroup);
        e.setCreatedAt(OffsetDateTime.now());
        return e;
    }
}

