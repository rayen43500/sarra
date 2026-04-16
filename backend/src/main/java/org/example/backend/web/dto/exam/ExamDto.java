package org.example.backend.web.dto.exam;

public record ExamDto(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        Double passScore,
        Boolean isActive
) {
}
