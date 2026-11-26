package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.ClinicalFinding;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Diagnosis;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Medication;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Treatment;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetCompletedClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ClinicalAttentionContextFacadeImpl implements ClinicalAttentionContextFacade {

  private static final ZoneId LIMA = ZoneId.of("America/Lima");

  private final ClinicalConsultationQueryService queryService;

  public ClinicalAttentionContextFacadeImpl(ClinicalConsultationQueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  public boolean existsConsultation(Long consultationId) {
    return find(consultationId).isPresent();
  }

  @Override
  public boolean isConsultationInProgress(Long consultationId) {
    return find(consultationId).map(ClinicalConsultation::isInProgress).orElse(false);
  }

  @Override
  public Long fetchPetIdByConsultationId(Long consultationId) {
    return find(consultationId).map(consultation -> consultation.getPetId().petId()).orElse(0L);
  }

  @Override
  public Long fetchVeterinarianIdByConsultationId(Long consultationId) {
    return find(consultationId)
            .map(consultation -> consultation.getVeterinarianId().veterinarianId())
            .orElse(0L);
  }

  @Override
  public Long fetchClinicIdByConsultationId(Long consultationId) {
    return find(consultationId)
            .map(consultation -> consultation.getClinicId().clinicId())
            .orElse(0L);
  }

  @Override
  public Optional<ConsultationSummary> fetchConsultationSummary(Long consultationId) {
    return find(consultationId).map(ClinicalAttentionContextFacadeImpl::toSummary);
  }

  @Override
  public List<ConsultationSummary> fetchCompletedConsultationSummariesByPetId(Long petId) {
    if (petId == null || petId <= 0) {
      return List.of();
    }
    return queryService.handle(new GetCompletedClinicalConsultationsByPetIdQuery(petId)).stream()
        .map(ClinicalAttentionContextFacadeImpl::toSummary)
        .toList();
  }

  private static ConsultationSummary toSummary(ClinicalConsultation consultation) {
    Instant moment = consultation.getCompletedAt() != null
        ? consultation.getCompletedAt()
        : consultation.getCreatedAt();
    LocalDate date = moment == null ? null : LocalDate.ofInstant(moment, LIMA);
    return new ConsultationSummary(consultation.getId(), consultation.getPetId().petId(),
        consultation.getClinicId().clinicId(), consultation.getStatus().name(), date,
        consultation.getReason(), findings(consultation), diagnosis(consultation),
        treatment(consultation));
  }

  private static String findings(ClinicalConsultation consultation) {
    var parts = new ArrayList<String>();
    if (consultation.getAnamnesis() != null) {
      parts.add("Motivo referido: " + consultation.getAnamnesis().getChiefComplaint());
    }
    consultation.getClinicalFindings().stream()
        .map(ClinicalFinding::getDescription)
        .forEach(parts::add);
    return parts.isEmpty() ? null : String.join("; ", parts);
  }

  private static String diagnosis(ClinicalConsultation consultation) {
    return consultation.getDiagnoses().stream()
        .filter(diagnosis -> diagnosis.isPrimary() && diagnosis.isCurrent())
        .map(Diagnosis::getDescription)
        .findFirst()
        .orElse(null);
  }

  private static String treatment(ClinicalConsultation consultation) {
    var parts = new ArrayList<String>();
    consultation.getTreatments().stream()
        .map(Treatment::getDescription)
        .forEach(parts::add);
    consultation.getPrescriptions().forEach(prescription -> prescription.getMedications()
        .stream()
        .map(ClinicalAttentionContextFacadeImpl::describe)
        .forEach(parts::add));
    return parts.isEmpty() ? null : parts.stream().collect(Collectors.joining("; "));
  }

  private static String describe(Medication medication) {
    return "%s %s, %s, %s".formatted(medication.getName(), medication.getDosage(),
        medication.getFrequency(), medication.getDuration());
  }

  private Optional<ClinicalConsultation> find(Long consultationId) {
    if (consultationId == null || consultationId <= 0) {
      return Optional.empty();
    }
    return queryService.handle(new GetClinicalConsultationByIdQuery(consultationId));
  }
}
