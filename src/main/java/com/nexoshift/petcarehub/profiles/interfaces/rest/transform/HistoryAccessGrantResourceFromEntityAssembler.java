package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.HistoryAccessGrantResource;

import java.time.Instant;

public final class HistoryAccessGrantResourceFromEntityAssembler {

    private HistoryAccessGrantResourceFromEntityAssembler() {
    }

    public static HistoryAccessGrantResource toResourceFromEntity(HistoryAccessGrant grant) {
        return new HistoryAccessGrantResource(
                grant.getId(),
                grant.getPetId().petId(),
                grant.getClinicId().clinicId(),
                grant.getGrantedAt(),
                grant.getExpiresAt(),
                grant.getRevokedAt(),
                grant.statusAt(Instant.now()));
    }
}
