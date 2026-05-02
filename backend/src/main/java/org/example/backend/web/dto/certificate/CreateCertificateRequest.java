package org.example.backend.web.dto.certificate;

import java.time.LocalDate;

public record CreateCertificateRequest(
        String title,
        String description,
        Long issuedToUserId,
        LocalDate issueDate,
        LocalDate expiryDate
) {
}
