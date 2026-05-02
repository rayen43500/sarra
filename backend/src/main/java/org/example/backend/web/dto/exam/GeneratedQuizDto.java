package org.example.backend.web.dto.exam;

import java.util.List;

public record GeneratedQuizDto(
        ExamDto exam,
        List<ExamQuestionDto> questions,
        String source
) {
}
