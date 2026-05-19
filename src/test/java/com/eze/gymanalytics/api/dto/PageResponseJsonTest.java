package com.eze.gymanalytics.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesPageResponseToExpectedJsonShape() throws Exception {
        PageResponse<String> response = new PageResponse<>(List.of("a", "b"), 1, 20, 45L, 3);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"content\":[\"a\",\"b\"]");
        assertThat(json).contains("\"page\":1");
        assertThat(json).contains("\"size\":20");
        assertThat(json).contains("\"totalElements\":45");
        assertThat(json).contains("\"totalPages\":3");
    }
}
