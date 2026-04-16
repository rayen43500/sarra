package org.example.backend.web.dto.exam;

import jakarta.validation.constraints.NotBlank;

public record CreateExamQuestionOptionRequest(
        @NotBlank String optionText,
        Boolean isCorrect
) {
}
