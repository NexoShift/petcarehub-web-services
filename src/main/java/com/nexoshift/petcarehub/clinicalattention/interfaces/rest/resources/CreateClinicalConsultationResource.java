package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** The veterinarian in charge is the authenticated user, so it is not part of the body. */
public record CreateClinicalConsultationResource(
    @NotNull @Positive Long petId,
    @NotNull @Positive Long clinicId,
    @Positive Long consultingRoomId,
    @NotBlank @Size(max = ClinicalFieldLimits.REASON) String reason) {
}
