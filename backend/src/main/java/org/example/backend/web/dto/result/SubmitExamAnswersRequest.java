package org.example.backend.web.dto.result;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubmitExamAnswersRequest(
        @NotNull Long examId,
        Integer attemptNumber,
        @Valid @NotEmpty List<SubmitExamAnswerItemRequest> answers
) {
}
