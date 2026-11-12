package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource containing the information required to create a consulting room.
 */
public record CreateConsultingRoomResource(
        String name,
        String description
) {
}
