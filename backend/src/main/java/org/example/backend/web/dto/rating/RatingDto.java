package org.example.backend.web.dto.rating;

import java.time.Instant;

public record RatingDto(
        Long id,
        Long userId,
        String targetType,
        Long targetId,
        Integer score,
        String comment,
        Instant createdAt
) {
}
