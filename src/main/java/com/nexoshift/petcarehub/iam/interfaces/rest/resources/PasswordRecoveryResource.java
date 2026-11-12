package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryResource(@NotBlank @Email String email) {
}
