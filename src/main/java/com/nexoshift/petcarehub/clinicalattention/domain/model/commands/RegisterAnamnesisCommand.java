package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import java.util.List;

/** The anamnesis is registered once per consultation: chief complaint and evolution time. */
public record RegisterAnamnesisCommand(
    Long consultationId, Long requesterUserId, String chiefComplaint, String evolutionTime,
    List<String> symptoms, String diet, String behavior, String previousTreatments,
    String ownerObservations) {

  public RegisterAnamnesisCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    chiefComplaint = CommandValidation.requiredText(
        chiefComplaint, ClinicalFieldLimits.CHIEF_COMPLAINT, "Chief complaint");
    evolutionTime = CommandValidation.requiredText(
        evolutionTime, ClinicalFieldLimits.EVOLUTION_TIME, "Evolution time");
    symptoms = CommandValidation.textList(
        symptoms, ClinicalFieldLimits.MAX_SYMPTOMS, ClinicalFieldLimits.SYMPTOM, "Symptoms");
    diet = CommandValidation.optionalText(diet, ClinicalFieldLimits.DIET, "Diet");
    behavior = CommandValidation.optionalText(behavior, ClinicalFieldLimits.BEHAVIOR, "Behavior");
    previousTreatments = CommandValidation.optionalText(
        previousTreatments, ClinicalFieldLimits.PREVIOUS_TREATMENTS, "Previous treatments");
    ownerObservations = CommandValidation.optionalText(
        ownerObservations, ClinicalFieldLimits.OWNER_OBSERVATIONS, "Owner observations");
  }
}
