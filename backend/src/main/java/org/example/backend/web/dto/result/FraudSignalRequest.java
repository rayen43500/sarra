package org.example.backend.web.dto.result;

import jakarta.validation.constraints.Min;

public record FraudSignalRequest(
        @Min(0) Integer focusLossCount,
        @Min(0) Integer copyPasteCount,
        @Min(0) Integer fullscreenExitCount,
        @Min(0) Integer totalSeconds,
        @Min(0) Integer remainingSeconds
) {
}
