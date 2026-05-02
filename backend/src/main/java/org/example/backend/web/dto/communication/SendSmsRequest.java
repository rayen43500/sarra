package org.example.backend.web.dto.communication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendSmsRequest(
        @NotBlank @Size(max = 30) String to,
        @NotBlank @Size(max = 1000) String message
) {
}
