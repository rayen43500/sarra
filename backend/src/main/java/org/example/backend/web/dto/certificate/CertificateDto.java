package org.example.backend.web.dto.certificate;

import java.time.LocalDate;

public record CertificateDto(
        Long id,
        String code,
        String title,
        String holder,
        LocalDate issueDate,
        LocalDate expiryDate,
        String status,
        String pdfPath
) {
}
