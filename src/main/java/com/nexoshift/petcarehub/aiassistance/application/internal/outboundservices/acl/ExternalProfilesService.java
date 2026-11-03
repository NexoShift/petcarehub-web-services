package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

import java.util.Optional;

/** Port to the Profiles context. Stub today, ProfilesContextFacade later. */
public interface ExternalProfilesService {

  Optional<PetSummary> fetchPetSummary(Long petId);

  /** True if the pet exists and belongs to that IAM user (ProfilesContextFacade later). */
  boolean isPetOwnedBy(Long petId, Long ownerUserId);
}
