package org.example.backend.web.dto.message;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long fromUserId,
        Long toUserId,
        String content,
        Boolean isRead,
        Instant createdAt
) {
}
