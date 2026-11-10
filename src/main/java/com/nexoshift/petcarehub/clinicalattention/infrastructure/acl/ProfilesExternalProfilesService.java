package com.nexoshift.petcarehub.clinicalattention.infrastructure.acl;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Adaptador REAL hacia Profiles &amp; Organization (reemplaza a LenientExternalProfilesService,
 * que decía "sí" a todo).
 * <p>
 * Traduce entre los tipos simples de {@link ProfileContextFacade} (Long, 0L = "no existe") y
 * los value objects de Clinical Attention (PetId, ClinicId, Optional...). Así el dominio de
 * Clinical Attention no depende de las clases internas de Profiles: solo de su fachada pública.
 */
@Service
public class ProfilesExternalProfilesService implements ExternalProfilesService {

  private final ProfileContextFacade profileContextFacade;

  public ProfilesExternalProfilesService(ProfileContextFacade profileContextFacade) {
    this.profileContextFacade = profileContextFacade;
  }

  @Override
  public boolean existsPet(PetId petId) {
    return profileContextFacade.existsPet(petId.petId());
  }

  /** El JWT trae el userId de IAM; aquí se convierte en el id del perfil veterinario. */
  @Override
  public Optional<VeterinarianId> fetchVeterinarianIdByUserId(Long userId) {
    return positive(profileContextFacade.fetchVeterinarianIdByUserId(userId))
        .map(VeterinarianId::new);
  }

  /** Un veterinario está activo en la clínica cuando su perfil pertenece a esa clínica. */
  @Override
  public boolean isVeterinarianActiveInClinic(VeterinarianId veterinarianId, ClinicId clinicId) {
    return fetchClinicIdByVeterinarianId(veterinarianId)
        .map(clinicId::equals)
        .orElse(false);
  }

  @Override
  public boolean isConsultingRoomActive(ClinicId clinicId, ConsultingRoomId consultingRoomId) {
    return profileContextFacade.existsActiveConsultingRoom(
        clinicId.clinicId(), consultingRoomId.consultingRoomId());
  }

  /** Mascota -> dueño (perfil) -> usuario de IAM del dueño. */
  @Override
  public Optional<Long> fetchPetOwnerUserIdByPetId(PetId petId) {
    return positive(profileContextFacade.fetchPetOwnerIdByPetId(petId.petId()))
        .flatMap(ownerId -> positive(profileContextFacade.fetchUserIdByPetOwnerId(ownerId)));
  }

  @Override
  public boolean isPetPatientOfClinic(PetId petId, ClinicId clinicId) {
    return profileContextFacade.isPetPatientOfClinic(clinicId.clinicId(), petId.petId());
  }

  @Override
  public Optional<ClinicId> fetchClinicIdByVeterinarianId(VeterinarianId veterinarianId) {
    return positive(
        profileContextFacade.fetchClinicIdByVeterinarianId(veterinarianId.veterinarianId()))
        .map(ClinicId::new);
  }

  /** La fachada de Profiles devuelve 0L (o null) cuando no encuentra algo. */
  private static Optional<Long> positive(Long value) {
    return value == null || value <= 0 ? Optional.empty() : Optional.of(value);
  }
}
