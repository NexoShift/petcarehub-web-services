package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import jakarta.validation.constraints.*;

public record SignUpResource(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull AccountType accountType,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Pattern(regexp = "^\\+51\\d{9}$", message = "Phone must be +51 followed by 9 digits") String phoneNumber,
        @Size(max = 100) String clinicName,
        @Pattern(regexp = "^\\d{11}$", message = "RUC must have 11 digits") String ruc) {
}
