package com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetAuthorizedClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.repositories.ClinicalConsultationRepository;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lecturas de consultas. Regla de negocio de acceso (la historia clínica es sensible):
 * <ul>
 *   <li>El dueño de la mascota ve todas las consultas de su mascota.</li>
 *   <li>Un veterinario ve las consultas de las mascotas que son pacientes de SU clínica
 *       (incluidas las hechas en otras clínicas: así conoce los antecedentes).</li>
 *   <li>Un veterinario también ve una consulta hecha en su propia clínica.</li>
 *   <li>Cualquier otro usuario recibe 403.</li>
 * </ul>
 */
@Service
public class ClinicalConsultationQueryServiceImpl implements ClinicalConsultationQueryService {

  private final ClinicalConsultationRepository consultationRepository;
  private final ExternalProfilesService externalProfilesService;

  public ClinicalConsultationQueryServiceImpl(
          ClinicalConsultationRepository consultationRepository,
          ExternalProfilesService externalProfilesService) {
    this.consultationRepository = consultationRepository;
    this.externalProfilesService = externalProfilesService;
  }

  /** Interno: lo usa ClinicalAttentionContextFacade. No exponer en un controller. */
  @Override
  @Transactional(readOnly = true)
  public Optional<ClinicalConsultation> handle(GetClinicalConsultationByIdQuery query) {
    return consultationRepository.findById(query.consultationId());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClinicalConsultation> handle(GetAuthorizedClinicalConsultationByIdQuery query) {
    var consultation = consultationRepository.findById(query.consultationId());
    consultation.ifPresent(found -> {
      if (!canRead(found.getPetId(), found.getClinicId(), query.requesterUserId())) {
        throw new ForbiddenException("You cannot see this clinical consultation");
      }
    });
    return consultation;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClinicalConsultation> handle(GetClinicalConsultationsByPetIdQuery query) {
    var petId = new PetId(query.petId());
    if (!externalProfilesService.existsPet(petId)) {
      throw new PetNotFoundException(query.petId());
    }
    if (!canRead(petId, null, query.requesterUserId())) {
      throw new ForbiddenException("Only the owner of the pet or a veterinarian of a clinic "
              + "where the pet is a patient can see its clinical consultations");
    }
    return consultationRepository.findAllByPetIdOrderByCreatedAtDesc(petId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClinicalConsultation> handle(GetClinicalConsultationsByVeterinarianIdQuery query) {
    return consultationRepository.findAllByVeterinarianIdOrderByCreatedAtDesc(
            new VeterinarianId(query.veterinarianId()));
  }

  /**
   * @param consultationClinicId clínica de la consulta concreta, o null cuando se lista todo
   *                             el historial de la mascota
   */
  private boolean canRead(PetId petId, ClinicId consultationClinicId, Long requesterUserId) {
    // 1) Dueño de la mascota
    var isOwner = externalProfilesService.fetchPetOwnerUserIdByPetId(petId)
            .filter(ownerUserId -> ownerUserId.equals(requesterUserId))
            .isPresent();
    if (isOwner) {
      return true;
    }
    // 2) Veterinario: se busca su clínica (userId -> perfil vet -> clínica)
    var veterinarianClinic = externalProfilesService.fetchVeterinarianIdByUserId(requesterUserId)
            .flatMap(externalProfilesService::fetchClinicIdByVeterinarianId);
    if (veterinarianClinic.isEmpty()) {
      return false;
    }
    var clinicId = veterinarianClinic.get();
    return clinicId.equals(consultationClinicId)
            || externalProfilesService.isPetPatientOfClinic(petId, clinicId);
  }
}
