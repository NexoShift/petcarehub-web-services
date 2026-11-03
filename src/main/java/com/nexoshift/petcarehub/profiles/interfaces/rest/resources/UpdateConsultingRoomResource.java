package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource containing the information required to update a consulting room.
 */
public record UpdateConsultingRoomResource(
        String name,
        String description
) {
}
