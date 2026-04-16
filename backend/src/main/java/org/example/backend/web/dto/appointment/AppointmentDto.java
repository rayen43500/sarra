package org.example.backend.web.dto.appointment;

import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentDto(
        Long id,
        Long userId,
        LocalDateTime scheduledAt,
        String note,
        String status,
        Instant createdAt
) {
}
