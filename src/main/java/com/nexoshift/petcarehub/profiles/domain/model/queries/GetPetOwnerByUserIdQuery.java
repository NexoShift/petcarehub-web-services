package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a pet owner by its IAM user identifier.
 *
 * @param userId the IAM user identifier
 */
public record GetPetOwnerByUserIdQuery(Long userId) {

    public GetPetOwnerByUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be a positive number");
        }
    }
}