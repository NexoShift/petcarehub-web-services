package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetHistoryAccessGrantsByPetIdQuery;

import java.util.List;

public interface HistoryAccessGrantQueryService {

    List<HistoryAccessGrant> handle(GetHistoryAccessGrantsByPetIdQuery query);

    /** True while the owner lets this clinic see the records of other clinics. */
    boolean hasActiveAccess(Long clinicId, Long petId);
}
