package org.example.backend.web.dto.notification;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String title,
        String message,
        String type,
        boolean isRead,
        Instant createdAt
) {
}
