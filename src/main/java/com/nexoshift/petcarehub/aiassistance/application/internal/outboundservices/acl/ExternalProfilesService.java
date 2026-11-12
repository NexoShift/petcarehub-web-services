package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Port to the Profiles context.
 * Implementaciones: facade/ProfilesServiceFacade (real, por defecto) y stub/ProfilesServiceStub
 * (mascotas demo, solo con AI_ACL_MODE=stub).
 */
public interface ExternalProfilesService {

  Optional<PetSummary> fetchPetSummary(Long petId);

  /** True if the pet exists and belongs to that IAM user. */
  boolean isPetOwnedBy(Long petId, Long ownerUserId);

  /**
   * NUEVO. True si el usuario de IAM tiene perfil de veterinario y la mascota es paciente de
   * la clínica de ese veterinario. Un vet no puede analizar ni ver imágenes de mascotas ajenas
   * a su clínica.
   */
  boolean isPetPatientOfVeterinarianClinic(Long petId, Long veterinarianUserId);
}
