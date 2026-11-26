package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.entities.SoapDraft;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.ClinicalAssistanceResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.SelectedBackgroundResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.SoapDraftResource;

public final class ClinicalAssistanceResourceFromEntityAssembler {

  private static final String BASE_PATH = "/api/v1/ai/clinical-assistances/";

  private ClinicalAssistanceResourceFromEntityAssembler() {
  }

  public static ClinicalAssistanceResource toResourceFromEntity(ClinicalAssistance entity) {
    var backgrounds = entity.getSelectedBackgrounds().stream()
        .map(background -> new SelectedBackgroundResource(
            background.type(), background.referenceId()))
        .toList();
    var drafts = entity.getDrafts().stream()
        .map(draft -> toDraftResource(entity.getId(), draft))
        .toList();
    return new ClinicalAssistanceResource(entity.getId(), entity.getConsultationId(),
        entity.getPetId(), entity.getVeterinarianId(), entity.getCurrentReason(),
        entity.getStatus().name(), entity.getFailureReason(), backgrounds, drafts,
        entity.getCreatedAt());
  }

  private static SoapDraftResource toDraftResource(Long assistanceId, SoapDraft draft) {
    var reportUrl = draft.hasReport()
        ? BASE_PATH + assistanceId + "/drafts/" + draft.getId() + "/report"
        : null;
    return new SoapDraftResource(draft.getId(), draft.getSubjective(), draft.getObjective(),
        draft.getAssessment(), draft.getPlan(), draft.getWarnings(), draft.getModel(),
        draft.getDecision().name(), draft.getEditedText(), reportUrl, draft.getCreatedAt());
  }
}
