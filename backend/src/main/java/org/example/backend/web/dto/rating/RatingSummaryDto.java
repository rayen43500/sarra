package org.example.backend.web.dto.rating;

import java.util.List;

public record RatingSummaryDto(
        String targetType,
        Long targetId,
        double average,
        long total,
        List<RatingDto> latestRatings
) {
}
