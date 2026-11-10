package com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.util.Optional;

/**
 * Anti-corruption layer towards Profiles &amp; Organization. It answers in terms of this context
 * (value objects) what the ProfileContextFacade answers with simple types.
 * <p>
 * Es un "puerto de salida": la capa de aplicación solo conoce esta interfaz. La implementación
 * real está en infrastructure/acl/ProfilesExternalProfilesService y en los tests se usa
 * FakeExternalProfilesService.
 */
public interface ExternalProfilesService {

  boolean existsPet(PetId petId);

  /** Id del perfil de veterinario del usuario de IAM. Empty when the user is not a veterinarian. */
  Optional<VeterinarianId> fetchVeterinarianIdByUserId(Long userId);

  boolean isVeterinarianActiveInClinic(VeterinarianId veterinarianId, ClinicId clinicId);

  boolean isConsultingRoomActive(ClinicId clinicId, ConsultingRoomId consultingRoomId);

  /** User id (IAM) of the owner of the pet, empty when it is not known. */
  Optional<Long> fetchPetOwnerUserIdByPetId(PetId petId);

  /** NUEVO: true si la mascota está registrada como paciente de la clínica. */
  boolean isPetPatientOfClinic(PetId petId, ClinicId clinicId);

  /** NUEVO: clínica donde trabaja el veterinario. Empty when the veterinarian does not exist. */
  Optional<ClinicId> fetchClinicIdByVeterinarianId(VeterinarianId veterinarianId);
}
