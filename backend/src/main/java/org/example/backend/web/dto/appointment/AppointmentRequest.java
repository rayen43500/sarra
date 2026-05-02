package org.example.backend.web.dto.appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull LocalDateTime scheduledAt,
        @Size(max = 1000) String note
) {
}
