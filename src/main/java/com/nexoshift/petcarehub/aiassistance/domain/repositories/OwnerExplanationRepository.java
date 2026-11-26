package com.nexoshift.petcarehub.aiassistance.domain.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.UserId;
import java.util.List;

public interface OwnerExplanationRepository {

  OwnerExplanation save(OwnerExplanation ownerExplanation);

  /** Oldest first, to paint the chat. */
  List<OwnerExplanation> findByPetIdAndOwnerIdOrderByIdAsc(PetId petId, UserId ownerId);
}
