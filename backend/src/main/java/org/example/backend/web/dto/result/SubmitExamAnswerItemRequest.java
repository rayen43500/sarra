package org.example.backend.web.dto.result;

import jakarta.validation.constraints.NotNull;

public record SubmitExamAnswerItemRequest(
        @NotNull Long questionId,
        @NotNull Long optionId
) {
}
