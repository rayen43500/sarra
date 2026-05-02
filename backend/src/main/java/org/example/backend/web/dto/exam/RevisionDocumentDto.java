package org.example.backend.web.dto.exam;

public record RevisionDocumentDto(
        Long examId,
        String title,
        String url
) {
}
