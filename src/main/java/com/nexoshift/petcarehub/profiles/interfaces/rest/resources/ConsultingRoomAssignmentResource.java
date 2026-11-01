package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import java.time.Instant;

/**
 * Resource representing a consulting room assignment.
 */
public record ConsultingRoomAssignmentResource(
        Long id,
        Long consultingRoomId,
        Long veterinarianId,
        Instant assignedAt,
        Instant unassignedAt,
        boolean active
) {
}
