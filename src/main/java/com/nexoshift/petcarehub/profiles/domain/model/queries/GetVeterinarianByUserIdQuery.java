package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a veterinarian by its IAM user identifier.
 *
 * @param userId the IAM user identifier
 */
public record GetVeterinarianByUserIdQuery(Long userId) {

    public GetVeterinarianByUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be a positive number");
        }
    }
}
