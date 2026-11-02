package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoom;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ConsultingRoomResource;

/**
 * Assembler for converting a consulting room entity into a resource.
 */
public class ConsultingRoomResourceFromEntityAssembler {

    public static ConsultingRoomResource toResourceFromEntity(
            ConsultingRoom entity) {

        return new ConsultingRoomResource(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive()
        );
    }
}
