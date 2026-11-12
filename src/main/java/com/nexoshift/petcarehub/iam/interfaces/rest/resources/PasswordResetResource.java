package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetResource(
    @NotBlank String token, @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
