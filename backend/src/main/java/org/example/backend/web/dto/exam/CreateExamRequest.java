package org.example.backend.web.dto.exam;

public record CreateExamRequest(
        String title,
        String description,
        Integer durationMinutes,
        Double passScore,
        Boolean isActive
) {
}
