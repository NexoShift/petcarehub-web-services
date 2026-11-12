package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource representing a consulting room.
 */
public record ConsultingRoomResource(
        Long id,
        String name,
        String description,
        boolean active
) {
}
