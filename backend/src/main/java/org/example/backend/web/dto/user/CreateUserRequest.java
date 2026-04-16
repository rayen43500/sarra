package org.example.backend.web.dto.user;

import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @Size(min = 6) String password,
        String phone,
        RoleName role,
        UserStatus status
) {
}
