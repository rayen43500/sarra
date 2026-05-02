package org.example.backend.web.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateExamQuestionRequest(
        @NotBlank String questionText,
        Double points,
        Integer orderIndex,
        @Valid @NotEmpty List<CreateExamQuestionOptionRequest> options
) {
}
