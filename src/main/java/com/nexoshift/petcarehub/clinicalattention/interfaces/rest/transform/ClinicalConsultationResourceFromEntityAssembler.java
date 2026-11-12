package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Anamnesis;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.ClinicalFinding;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.PhysiologicalAssessment;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.AnamnesisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ClinicalFindingResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.PhysiologicalAssessmentResource;

public final class ClinicalConsultationResourceFromEntityAssembler {

  private ClinicalConsultationResourceFromEntityAssembler() {
  }

  public static ClinicalConsultationResource toResourceFromEntity(
      ClinicalConsultation consultation) {
    var cancellation = consultation.getCancellation();
    var consultingRoomId = consultation.getConsultingRoomId();
    return new ClinicalConsultationResource(
        consultation.getId(),
        consultation.getPetId().petId(),
        consultation.getVeterinarianId().veterinarianId(),
        consultation.getClinicId().clinicId(),
        consultingRoomId == null ? null : consultingRoomId.consultingRoomId(),
        consultation.getReason(),
        consultation.getStatus(),
        consultation.getStartedAt(),
        consultation.getCompletedAt(),
        cancellation == null ? null : cancellation.reason(),
        cancellation == null ? null : cancellation.cancelledAt(),
        consultation.getAnamnesis() == null ? null : toResource(consultation.getAnamnesis()),
        consultation.getPhysiologicalAssessments().stream()
            .map(ClinicalConsultationResourceFromEntityAssembler::toResource).toList(),
        consultation.getClinicalFindings().stream()
            .map(ClinicalConsultationResourceFromEntityAssembler::toResource).toList(),
        consultation.getDiagnoses().stream()
            .map(DiagnosisResourceFromEntityAssembler::toResourceFromEntity).toList(),
        consultation.getCreatedAt(),
        consultation.getUpdatedAt());
  }

  private static AnamnesisResource toResource(Anamnesis anamnesis) {
    return new AnamnesisResource(anamnesis.getId(), anamnesis.getChiefComplaint(),
        anamnesis.getEvolutionTime(), anamnesis.getSymptoms(), anamnesis.getDiet(),
        anamnesis.getBehavior(), anamnesis.getPreviousTreatments(),
        anamnesis.getOwnerObservations());
  }

  private static PhysiologicalAssessmentResource toResource(PhysiologicalAssessment assessment) {
    var vitalSigns = assessment.getVitalSigns();
    var bodyConditionScore = assessment.getBodyConditionScore();
    return new PhysiologicalAssessmentResource(assessment.getId(), vitalSigns.weightKg(),
        vitalSigns.temperatureCelsius(), vitalSigns.heartRateBpm(), vitalSigns.respiratoryRateBpm(),
        bodyConditionScore == null ? null : bodyConditionScore.score(),
        assessment.getHydrationStatus(), assessment.getMucousMembranes(),
        assessment.getCapillaryRefillTimeSeconds(), assessment.getObservations(),
        assessment.getRecordedAt());
  }

  private static ClinicalFindingResource toResource(ClinicalFinding finding) {
    return new ClinicalFindingResource(finding.getId(), finding.getBodySystem(),
        finding.getDescription(), finding.getSeverity(), finding.getCreatedAt());
  }
}
