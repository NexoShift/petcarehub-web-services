package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import java.util.Map;
import java.util.Optional;

/**
 * Test double of the Profiles port with two fixed pets (Rocky and Luna). Unit tests create
 * it with new; it is not a Spring bean and it is not part of the application.
 */
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

  /** Demo: cualquier veterinario atiende a Rocky y Luna. */
  @Override
  public boolean isPetPatientOfVeterinarianClinic(Long petId, Long veterinarianUserId) {
    return PETS.containsKey(petId) && veterinarianUserId != null;
  }

  /** Demo: every veterinarian works in clinic 1. */
  @Override
  public Optional<Long> fetchVeterinarianClinicId(Long veterinarianUserId) {
    return veterinarianUserId == null ? Optional.empty() : Optional.of(1L);
  }

  /** Demo: the owner already gave access, so the whole history is visible. */
  @Override
  public boolean hasActiveHistoryAccess(Long clinicId, Long petId) {
    return PETS.containsKey(petId);
  }
}
