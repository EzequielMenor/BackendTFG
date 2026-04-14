package com.eze.gymanalytics.api.service;

import com.eze.gymanalytics.api.repository.SerieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SerieRepository serieRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    // ─────────────────────────────────────────────────────
    // getSummary() — stub
    // ─────────────────────────────────────────────────────

    @Test
    void getSummary_stub_throwsUnsupportedOperation() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.now().minusDays(30);
        OffsetDateTime to   = OffsetDateTime.now();

        assertThatThrownBy(() -> analyticsService.getSummary(userId, from, to))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("getSummary not yet implemented");
    }
}
