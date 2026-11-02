package com.nexoshift.petcarehub.clinicalattention.infrastructure.acl;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * TEMPORARY adapter: Profiles &amp; Organization is not merged yet. Every pet, clinic and room is
 * accepted, and the id of the user is used as the id of the veterinarian. Replace it with the
 * ExternalProfilesService that calls ProfilesContextFacade (and delete this class) as soon as
 * that context is available.
 */
@Service
public class LenientExternalProfilesService implements ExternalProfilesService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LenientExternalProfilesService.class);

  public LenientExternalProfilesService() {
    LOGGER.warn("Profiles & Organization is not integrated: pets, clinics and rooms are not "
        + "validated and the user id is used as the veterinarian id");
  }

  @Override
  public boolean existsPet(PetId petId) {
    return true;
  }

  @Override
  public Optional<VeterinarianId> fetchVeterinarianIdByUserId(Long userId) {
    return userId == null ? Optional.empty() : Optional.of(new VeterinarianId(userId));
  }

  @Override
  public boolean isVeterinarianActiveInClinic(VeterinarianId veterinarianId, ClinicId clinicId) {
    return true;
  }

  @Override
  public boolean isConsultingRoomActive(ClinicId clinicId, ConsultingRoomId consultingRoomId) {
    return true;
  }

  @Override
  public Optional<Long> fetchPetOwnerUserIdByPetId(PetId petId) {
    return Optional.empty();
  }
}
