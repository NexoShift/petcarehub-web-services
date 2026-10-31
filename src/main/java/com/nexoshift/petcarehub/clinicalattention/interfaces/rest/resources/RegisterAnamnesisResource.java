package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RegisterAnamnesisResource(
    @NotBlank @Size(max = ClinicalFieldLimits.CHIEF_COMPLAINT) String chiefComplaint,
    @NotBlank @Size(max = ClinicalFieldLimits.EVOLUTION_TIME) String evolutionTime,
    @Size(max = ClinicalFieldLimits.MAX_SYMPTOMS)
    List<@NotBlank @Size(max = ClinicalFieldLimits.SYMPTOM) String> symptoms,
    @Size(max = ClinicalFieldLimits.DIET) String diet,
    @Size(max = ClinicalFieldLimits.BEHAVIOR) String behavior,
    @Size(max = ClinicalFieldLimits.PREVIOUS_TREATMENTS) String previousTreatments,
    @Size(max = ClinicalFieldLimits.OWNER_OBSERVATIONS) String ownerObservations) {
}
