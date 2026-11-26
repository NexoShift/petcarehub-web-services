package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import java.util.List;

/** Vaccinations and entries come most recent first. */
public record MedicalHistoryResource(Long petId, List<AllergyResource> allergies,
                                     List<VaccinationResource> vaccinations,
                                     List<MedicalHistoryEntryResource> entries) {
}
