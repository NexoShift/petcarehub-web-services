package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Step 1 of the onboarding. IAM only receives the credentials and the account type.
 * Personal data (names, phone, clinic) is sent to Profiles in step 2.
 */
public record SignUpResource(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull AccountType accountType) {
}
