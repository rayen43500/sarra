package org.example.backend.web.dto.communication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendEmailRequest(
        @Email @NotBlank String to,
        @NotBlank @Size(max = 200) String subject,
        @NotBlank @Size(max = 5000) String body
) {
}
