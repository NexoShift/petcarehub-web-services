package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * @param idToken the "credential" that Google Identity Services gives the frontend
 * @param accountType PET_OWNER or CLINIC; only needed the first time (to create the account)
 */
public record GoogleSignInResource(@NotBlank String idToken, String accountType) {
}
