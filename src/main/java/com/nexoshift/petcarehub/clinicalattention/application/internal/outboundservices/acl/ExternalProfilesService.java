package com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.util.Optional;

/**
 * Anti-corruption layer towards Profiles &amp; Organization. It answers in terms of this context
 * (value objects) what the ProfilesContextFacade answers with simple types.
 */
public interface ExternalProfilesService {

  boolean existsPet(PetId petId);

  /** Empty when the user is not a registered veterinarian. */
  Optional<VeterinarianId> fetchVeterinarianIdByUserId(Long userId);

  boolean isVeterinarianActiveInClinic(VeterinarianId veterinarianId, ClinicId clinicId);

  boolean isConsultingRoomActive(ClinicId clinicId, ConsultingRoomId consultingRoomId);

  /** User id of the owner of the pet, empty when it is not known. */
  Optional<Long> fetchPetOwnerUserIdByPetId(PetId petId);
}
