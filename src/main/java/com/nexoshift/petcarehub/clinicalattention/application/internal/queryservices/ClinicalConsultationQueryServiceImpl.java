package com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.repositories.ClinicalConsultationRepository;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Override
  @Transactional(readOnly = true)
  public Optional<ClinicalConsultation> handle(GetClinicalConsultationByIdQuery query) {
    return consultationRepository.findById(query.consultationId());
  }

  /** Allowed for any veterinarian and for the owner of the pet. */
  @Override
  @Transactional(readOnly = true)
  public List<ClinicalConsultation> handle(GetClinicalConsultationsByPetIdQuery query) {
    var petId = new PetId(query.petId());
    if (!externalProfilesService.existsPet(petId)) {
      throw new PetNotFoundException(query.petId());
    }
    var isOwner = externalProfilesService.fetchPetOwnerUserIdByPetId(petId)
        .filter(ownerUserId -> ownerUserId.equals(query.requesterUserId()))
        .isPresent();
    var isVeterinarian =
        externalProfilesService.fetchVeterinarianIdByUserId(query.requesterUserId()).isPresent();
    if (!isOwner && !isVeterinarian) {
      throw new ForbiddenException(
          "Only a veterinarian or the owner of the pet can see its clinical consultations");
    }
    return consultationRepository.findAllByPetIdOrderByCreatedAtDesc(petId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClinicalConsultation> handle(GetClinicalConsultationsByVeterinarianIdQuery query) {
    return consultationRepository.findAllByVeterinarianIdOrderByCreatedAtDesc(
        new VeterinarianId(query.veterinarianId()));
  }
}
