package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.AdminStatsDTO;
import com.eze.gymanalytics.api.dto.UserProfileDTO;
import com.eze.gymanalytics.api.model.Profile;
import com.eze.gymanalytics.api.repository.ExerciseRepository;
import com.eze.gymanalytics.api.repository.ProfileRepository;
import com.eze.gymanalytics.api.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ProfileRepository profileRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    public AdminService(ProfileRepository profileRepository,
                        WorkoutRepository workoutRepository,
                        ExerciseRepository exerciseRepository) {
        this.profileRepository = profileRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public AdminStatsDTO getStats() {
        long totalUsers = profileRepository.count();
        long totalWorkouts = workoutRepository.count();
        long totalExercises = exerciseRepository.count();
        long activeLastWeek = workoutRepository.countDistinctActiveUsersSince(
                OffsetDateTime.now().minusDays(7));

        return new AdminStatsDTO(totalUsers, totalWorkouts, totalExercises, activeLastWeek);
    }

    public List<UserProfileDTO> getUsers() {
        return profileRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private UserProfileDTO toDTO(Profile profile) {
        String createdAt = profile.getCreatedAt() != null
                ? profile.getCreatedAt().toString()
                : null;
        return new UserProfileDTO(
                profile.getId(),
                profile.getEmail(),
                profile.getUsername(),
                profile.getRole(),
                createdAt);
    }
}
