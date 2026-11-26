package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.facade;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.profiles.interfaces.acl.PetProfileSnapshot;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Real pets from the Profiles context (ACL).
 * Translates Profiles' snapshot into the anonymous PetSummary the AI context uses.
 */
@Service
public class ProfilesServiceFacade implements ExternalProfilesService {

  private final ProfileContextFacade profileContextFacade;

  public ProfilesServiceFacade(ProfileContextFacade profileContextFacade) {
    this.profileContextFacade = profileContextFacade;
  }

  @Override
  public Optional<PetSummary> fetchPetSummary(Long petId) {
    return profileContextFacade.fetchPetProfileSnapshot(petId).map(ProfilesServiceFacade::toSummary);
  }

  @Override
  public boolean isPetOwnedBy(Long petId, Long ownerUserId) {
    if (ownerUserId == null) {
      return false;
    }
    var petOwnerId = profileContextFacade.fetchPetOwnerIdByPetId(petId);
    if (petOwnerId == null || petOwnerId <= 0) {
      return false;
    }
    return ownerUserId.equals(profileContextFacade.fetchUserIdByPetOwnerId(petOwnerId));
  }

  /** userId (IAM) -> perfil vet -> clínica -> ¿la mascota es paciente de esa clínica? */
  @Override
  public boolean isPetPatientOfVeterinarianClinic(Long petId, Long veterinarianUserId) {
    if (petId == null || veterinarianUserId == null) {
      return false;
    }
    return fetchVeterinarianClinicId(veterinarianUserId)
        .map(clinicId -> profileContextFacade.isPetPatientOfClinic(clinicId, petId))
        .orElse(false);
  }

  @Override
  public Optional<Long> fetchVeterinarianClinicId(Long veterinarianUserId) {
    if (veterinarianUserId == null) {
      return Optional.empty();
    }
    var veterinarianId = profileContextFacade.fetchVeterinarianIdByUserId(veterinarianUserId);
    if (veterinarianId == null || veterinarianId <= 0) {
      return Optional.empty();
    }
    var clinicId = profileContextFacade.fetchClinicIdByVeterinarianId(veterinarianId);
    return clinicId == null || clinicId <= 0 ? Optional.empty() : Optional.of(clinicId);
  }

  @Override
  public boolean hasActiveHistoryAccess(Long clinicId, Long petId) {
    return profileContextFacade.hasActiveHistoryAccess(clinicId, petId);
  }

  private static PetSummary toSummary(PetProfileSnapshot pet) {
    return new PetSummary(pet.petId(), pet.name(), speciesOf(pet.species()), pet.breed(),
            sexOf(pet.sex()), ageInMonths(pet.birthDate()), "no registrado");
  }

  private static String speciesOf(String species) {
    if ("DOG".equals(species)) return "Perro";
    if ("CAT".equals(species)) return "Gato";
    return "Otra especie";
  }

  private static String sexOf(String sex) {
    if ("MALE".equals(sex)) return "macho";
    if ("FEMALE".equals(sex)) return "hembra";
    return "sexo no registrado";
  }

  private static Integer ageInMonths(LocalDate birthDate) {
    if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
      return null;
    }
    return (int) Period.between(birthDate, LocalDate.now()).toTotalMonths();
  }
}
