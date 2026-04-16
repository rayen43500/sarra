package org.example.backend.web.dto.result;

public record SubmitResultRequest(
        Long examId,
        Double score,
        Double maxScore,
        Integer attemptNumber
) {
}
