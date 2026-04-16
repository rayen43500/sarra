package org.example.backend.web.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RatingRequest(
        @NotBlank String targetType,
        @NotNull Long targetId,
        @NotNull @Min(1) @Max(5) Integer score,
        @Size(max = 1000) String comment
) {
}
