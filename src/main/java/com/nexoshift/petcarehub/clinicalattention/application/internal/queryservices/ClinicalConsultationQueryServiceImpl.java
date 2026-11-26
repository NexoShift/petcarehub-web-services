package com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetAuthorizedClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetCompletedClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
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
 *   <li>Un veterinario ve las consultas hechas en SU clínica.</li>
 *   <li>Las consultas de OTRAS clínicas solo las ve si la mascota es paciente de su clínica y
 *       el dueño le dio permiso por unas horas (HistoryAccessGrant en Profiles).</li>
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
  public List<ClinicalConsultation> handle(GetCompletedClinicalConsultationsByPetIdQuery query) {
    return consultationRepository.findAllByPetIdOrderByCreatedAtDesc(new PetId(query.petId()))
        .stream()
        .filter(consultation -> consultation.getStatus() == ConsultationStatus.COMPLETED)
        .toList();
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
    var consultations = consultationRepository.findAllByPetIdOrderByCreatedAtDesc(petId);
    if (isOwner(petId, query.requesterUserId())) {
      return consultations;
    }
    var clinicId = requesterClinic(query.requesterUserId())
            .filter(clinic -> externalProfilesService.isPetPatientOfClinic(petId, clinic))
            .orElseThrow(() -> new ForbiddenException("Only the owner of the pet or a "
                    + "veterinarian of a clinic where the pet is a patient can see its clinical "
                    + "consultations"));
    if (externalProfilesService.hasActiveHistoryAccess(petId, clinicId)) {
      return consultations;
    }
    // Without the owner's permission, the clinic only sees its own consultations.
    return consultations.stream()
            .filter(consultation -> clinicId.equals(consultation.getClinicId()))
            .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClinicalConsultation> handle(GetClinicalConsultationsByVeterinarianIdQuery query) {
    return consultationRepository.findAllByVeterinarianIdOrderByCreatedAtDesc(
            new VeterinarianId(query.veterinarianId()));
  }

  /** A consultation: its owner, its clinic, or another clinic with the owner's permission. */
  private boolean canRead(PetId petId, ClinicId consultationClinicId, Long requesterUserId) {
    if (isOwner(petId, requesterUserId)) {
      return true;
    }
    var clinicId = requesterClinic(requesterUserId);
    if (clinicId.isEmpty()) {
      return false;
    }
    return clinicId.get().equals(consultationClinicId)
            || externalProfilesService.hasActiveHistoryAccess(petId, clinicId.get());
  }

  private boolean isOwner(PetId petId, Long requesterUserId) {
    return externalProfilesService.fetchPetOwnerUserIdByPetId(petId)
            .filter(ownerUserId -> ownerUserId.equals(requesterUserId))
            .isPresent();
  }

  /** Clinic of the requester: userId -> veterinarian profile -> clinic. */
  private Optional<ClinicId> requesterClinic(Long requesterUserId) {
    return externalProfilesService.fetchVeterinarianIdByUserId(requesterUserId)
            .flatMap(externalProfilesService::fetchClinicIdByVeterinarianId);
  }
}
