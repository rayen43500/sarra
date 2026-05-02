package org.example.backend.web.dto.reaction;

public record ReactionSummaryDto(
        String targetType,
        Long targetId,
        long likes,
        long dislikes,
        Boolean myReaction
) {
}
