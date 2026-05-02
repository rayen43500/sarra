package org.example.backend.web.dto.verification;

import java.time.LocalDate;

public record CertificateVerificationResponse(
        String code,
        String holder,
        LocalDate issueDate,
        LocalDate expiryDate,
        String status,
        boolean valid
) {
}
