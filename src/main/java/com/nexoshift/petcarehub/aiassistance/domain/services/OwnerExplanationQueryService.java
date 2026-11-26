package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetOwnerExplanationsByPetIdQuery;
import java.util.List;

public interface OwnerExplanationQueryService {

  List<OwnerExplanation> handle(GetOwnerExplanationsByPetIdQuery query);
}
