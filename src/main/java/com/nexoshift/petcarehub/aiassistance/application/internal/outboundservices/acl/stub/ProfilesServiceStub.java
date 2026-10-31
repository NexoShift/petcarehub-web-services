package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Demo pets used until Profiles publishes its facade. There are no real owners yet, so
 * any pet owner account is treated as the owner of the demo pets (Rocky and Luna).
 */
@Service
@ConditionalOnProperty(name = "ai.acl.mode", havingValue = "stub", matchIfMissing = true)
public class ProfilesServiceStub implements ExternalProfilesService {

  private static final Map<Long, PetSummary> PETS = Map.of(
      1L, new PetSummary(1L, "Rocky", "Perro", "Bulldog francés", "macho", 38, "atigrado"),
      2L, new PetSummary(2L, "Luna", "Gato", "Doméstico de pelo corto", "hembra", 26, "gris"));

  @Override
  public Optional<PetSummary> fetchPetSummary(Long petId) {
    return Optional.ofNullable(PETS.get(petId));
  }

  @Override
  public boolean isPetOwnedBy(Long petId, Long ownerUserId) {
    return PETS.containsKey(petId) && ownerUserId != null;
  }
}
