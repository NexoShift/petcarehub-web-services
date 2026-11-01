package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoomAssignment;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ConsultingRoomAssignmentResource;

/**
 * Assembler for converting a consulting room assignment into a resource.
 */
public class ConsultingRoomAssignmentResourceFromEntityAssembler {

    public static ConsultingRoomAssignmentResource toResourceFromEntity(
            ConsultingRoomAssignment entity) {

        return new ConsultingRoomAssignmentResource(
                entity.getId(),
                entity.getConsultingRoomId().consultingRoomId(),
                entity.getVeterinarianId().veterinarianId(),
                entity.getAssignedAt(),
                entity.getUnassignedAt(),
                entity.isActive()
        );
    }
}