package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.dto.analytics.EffectiveVolumeDTO;
import com.eze.gymanalytics.api.dto.analytics.Progression1RMDTO;
import com.eze.gymanalytics.api.repository.SerieRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {

    private final SerieRepository serieRepository;

    public AnalyticsService(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    /**
     * Calculates the estimated 1RM progression for a given user and exercise,
     * using the Brzycki formula and filtering out warmup sets and sets with reps > 12.
     *
     * @param userId The ID of the user
     * @param exerciseId The ID of the exercise
     * @return List of progression points mapping Date to Estimated 1RM
     */
    public List<Progression1RMDTO> get1RMProgression(UUID userId, Long exerciseId) {
        return serieRepository.find1RMProgression(userId, exerciseId);
    }

    /**
     * Calculates the effective volume (number of effective sets) per muscle group
     * since a given start date. Filters out warmup sets and sets with RPE < 7.
     *
     * @param userId The ID of the user
     * @param startDate The date to start counting from (e.g., 30 days ago)
     * @return List mapping Muscle Group to Number of Effective Sets
     */
    public List<EffectiveVolumeDTO> getEffectiveVolume(UUID userId, OffsetDateTime startDate) {
        return serieRepository.findEffectiveVolume(userId, startDate);
    }
}
