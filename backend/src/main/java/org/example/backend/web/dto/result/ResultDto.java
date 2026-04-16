package org.example.backend.web.dto.result;

import java.time.Instant;

public record ResultDto(
        Long id,
        Long examId,
        String examTitle,
        Double score,
        Double maxScore,
        Double percentage,
        Boolean passed,
        Instant submittedAt
) {
}
