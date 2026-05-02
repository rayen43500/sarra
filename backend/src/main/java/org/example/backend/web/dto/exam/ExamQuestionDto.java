package org.example.backend.web.dto.exam;

import java.util.List;

public record ExamQuestionDto(
        Long id,
        String text,
        Double points,
        Integer orderIndex,
        List<ExamQuestionOptionDto> options
) {
}
