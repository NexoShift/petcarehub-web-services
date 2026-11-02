package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource containing the information required to assign a veterinarian.
 */
public record AssignVeterinarianResource(
        Long consultingRoomId,
        Long veterinarianId
) {
}
