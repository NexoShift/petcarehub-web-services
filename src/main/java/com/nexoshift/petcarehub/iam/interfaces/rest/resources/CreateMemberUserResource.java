package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request resource for registering a member user
 * within an existing clinic account.
 */
public record CreateMemberUserResource(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
