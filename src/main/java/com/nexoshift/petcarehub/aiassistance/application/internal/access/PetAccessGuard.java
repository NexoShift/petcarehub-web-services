package com.nexoshift.petcarehub.aiassistance.application.internal.access;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * NUEVO. Una sola regla de acceso a las imágenes/análisis de una mascota, usada al subir
 * (command service) y al leer (controller), para que no se desincronicen:
 * <ul>
 *   <li>PET_OWNER: solo sus propias mascotas.</li>
 *   <li>VETERINARIAN: solo mascotas que son pacientes de su clínica.</li>
 *   <li>Antecedentes de OTRAS clínicas: solo con el permiso por horas del dueño.</li>
 * </ul>
 */
@Component
public class PetAccessGuard {

  private final ExternalProfilesService profilesService;

  public PetAccessGuard(ExternalProfilesService profilesService) {
    this.profilesService = profilesService;
  }

  /** Lanza ForbiddenException (403) si el usuario no puede usar los datos de la mascota. */
  public void ensureCanAccessPet(Long petId, Long requesterUserId, RequesterRole role) {
    if (role == RequesterRole.PET_OWNER) {
      if (!profilesService.isPetOwnedBy(petId, requesterUserId)) {
        throw new ForbiddenException("You can only use the AI with your own pets");
      }
      return;
    }
    if (!profilesService.isPetPatientOfVeterinarianClinic(petId, requesterUserId)) {
      throw new ForbiddenException("The pet is not a patient of your clinic");
    }
  }

  /**
   * Which clinics' consultations a veterinarian may use as backgrounds: their own clinic, and
   * the others only while the owner's access grant is active. 403 if the pet is not a patient.
   */
  public Predicate<Long> visibleClinicsForVeterinarian(Long petId, Long veterinarianUserId) {
    ensureCanAccessPet(petId, veterinarianUserId, RequesterRole.VETERINARIAN);
    var clinicId = profilesService.fetchVeterinarianClinicId(veterinarianUserId)
        .orElseThrow(() -> new ForbiddenException("The pet is not a patient of your clinic"));
    if (profilesService.hasActiveHistoryAccess(clinicId, petId)) {
      return consultationClinicId -> true;
    }
    return clinicId::equals;
  }
}
