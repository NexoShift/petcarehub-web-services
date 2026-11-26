package com.nexoshift.petcarehub.medicalhistory.application.internal.access;

import com.nexoshift.petcarehub.medicalhistory.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.medicalhistory.domain.exceptions.MedicalHistoryAccessDeniedException;
import com.nexoshift.petcarehub.medicalhistory.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.ReporterType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Access rules of Medical History.
 * <ul>
 *   <li>Read and allergies: the owner of the pet or a veterinarian of a clinic where the pet is
 *   a patient.</li>
 *   <li>Vaccinations: only a veterinarian of a clinic where the pet is a patient.</li>
 * </ul>
 */
@Component
public class MedicalHistoryAccessGuard {

  /** Veterinarian of a clinic where the pet is a patient. */
  public record TreatingVeterinarian(Long veterinarianId, Long clinicId) {
  }

  private final ExternalProfilesService profiles;

  public MedicalHistoryAccessGuard(ExternalProfilesService profiles) {
    this.profiles = profiles;
  }

  public void requirePet(Long petId) {
    if (!profiles.existsPet(petId)) {
      throw new PetNotFoundException(petId);
    }
  }

  /** Who the user is for this pet: its owner or a treating veterinarian; 403 otherwise. */
  public ReporterType requireCanAccess(Long userId, Long petId) {
    requirePet(petId);
    if (isOwner(userId, petId)) {
      return ReporterType.OWNER;
    }
    if (treatingVeterinarian(userId, petId).isPresent()) {
      return ReporterType.VETERINARIAN;
    }
    throw new MedicalHistoryAccessDeniedException(
        "Only the owner or a veterinarian of a clinic where the pet is a patient can access"
            + " its medical history");
  }

  public TreatingVeterinarian requireTreatingVeterinarian(Long userId, Long petId) {
    requirePet(petId);
    return treatingVeterinarian(userId, petId)
        .orElseThrow(() -> new MedicalHistoryAccessDeniedException(
            "Only a veterinarian of a clinic where the pet is a patient can record vaccinations"));
  }

  private boolean isOwner(Long userId, Long petId) {
    return profiles.fetchOwnerUserIdByPetId(petId).map(userId::equals).orElse(false);
  }

  private Optional<TreatingVeterinarian> treatingVeterinarian(Long userId, Long petId) {
    return profiles.fetchVeterinarianIdByUserId(userId)
        .flatMap(veterinarianId -> profiles.fetchClinicIdByVeterinarianId(veterinarianId)
            .filter(clinicId -> profiles.isPetPatientOfClinic(clinicId, petId))
            .map(clinicId -> new TreatingVeterinarian(veterinarianId, clinicId)));
  }
}
