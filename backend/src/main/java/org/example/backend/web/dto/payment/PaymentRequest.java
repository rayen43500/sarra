package org.example.backend.web.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
        @NotNull @Min(1) Double amount,
        @Size(min = 3, max = 8) String currency,
        @Size(max = 300) String description
) {
}
