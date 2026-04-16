package org.example.backend.web.dto.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewsletterSubscriptionRequest(
        @Email @NotBlank String email
) {
}
