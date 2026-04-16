package org.example.backend.web.dto.log;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        String actor,
        String actionType,
        String entityType,
        String entityId,
        String details,
        String ipAddress,
        Instant createdAt
) {
}
