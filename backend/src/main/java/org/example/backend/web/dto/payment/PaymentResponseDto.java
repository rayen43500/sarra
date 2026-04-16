package org.example.backend.web.dto.payment;

import java.time.Instant;

public record PaymentResponseDto(
        Long id,
        String reference,
        String status,
        Double amount,
        String currency,
        String provider,
        Instant createdAt
) {
}
