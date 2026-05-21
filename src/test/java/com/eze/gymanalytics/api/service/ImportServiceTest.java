package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.model.Workout;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.SerieRepository;
import com.eze.gymanalytics.api.repository.WorkoutExerciseRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

  @Mock
  private WorkoutRepository workoutRepository;

  @Mock
  private ExerciseRepository exerciseRepository;

  @Mock
  private ProfileRepository profileRepository;

  @Mock
  private WorkoutExerciseRepository workoutExerciseRepository;

  @Mock
  private SerieRepository serieRepository;

  @Test
  void importHevyCsv_backfillsEndTimeOnExistingDuplicateWorkout() {
    ImportService importService = new ImportService(
        workoutRepository,
        exerciseRepository,
        profileRepository,
        workoutExerciseRepository,
        serieRepository
    );

    Profile user = new Profile();
    user.setId(UUID.randomUUID());
    user.setEmail("user@gym.com");

    Workout existingWorkout = new Workout();
    existingWorkout.setUser(user);
    existingWorkout.setName("TORSO B");
    existingWorkout.setStartTime(OffsetDateTime.of(2025, 11, 28, 6, 40, 0, 0, ZoneOffset.UTC));

    when(profileRepository.findByEmail("user@gym.com")).thenReturn(Optional.of(user));
    when(exerciseRepository.findAll()).thenReturn(List.of());
    when(workoutRepository.findByUserIdAndStartTimeBetweenAndName(
        eq(user.getId()),
        any(OffsetDateTime.class),
        any(OffsetDateTime.class),
        eq("TORSO B")
    )).thenReturn(Optional.of(existingWorkout));

    String csv = "\"title\",\"start_time\",\"end_time\",\"description\",\"exercise_title\",\"superset_id\",\"exercise_notes\",\"set_index\",\"set_type\",\"weight_kg\",\"reps\",\"distance_km\",\"duration_seconds\",\"rpe\"\n"
        + "\"TORSO B\",\"28 Nov 2025, 06:40\",\"28 Nov 2025, 07:21\",\"\",\"Bent Over Row (Barbell)\",,\"\",0,\"normal\",37.5,14,,,8.5\n";

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "hevy.csv",
        "text/csv",
        csv.getBytes(StandardCharsets.UTF_8)
    );

    importService.importHevyCsv(file, "user@gym.com");

    ArgumentCaptor<Iterable<Workout>> workoutsCaptor = ArgumentCaptor.forClass(Iterable.class);
    verify(workoutRepository).saveAll(workoutsCaptor.capture());

    assertThat(existingWorkout.getEndTime())
        .isEqualTo(OffsetDateTime.of(2025, 11, 28, 7, 21, 0, 0, ZoneOffset.UTC));
  }
}
