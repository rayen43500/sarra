package org.example.backend.web.dto.reaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(
        @NotBlank String targetType,
        @NotNull Long targetId,
        @NotNull Boolean liked
) {
}
