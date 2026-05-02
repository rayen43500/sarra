package org.example.backend.web.dto.exam;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GenerateQuizRequest(
        @NotBlank String topic,
        @Min(3) @Max(30) Integer questionCount,
        String difficulty,
        Integer durationMinutes,
        Double passScore,
        Boolean isActive
) {
}
