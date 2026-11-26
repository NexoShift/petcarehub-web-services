package com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects;

import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.Allergy;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.MedicalHistoryEntry;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.VaccinationRecord;

import java.util.List;

/**
 * The part of a medical history that the requester may see.
 * Allergies are always included; vaccinations and timeline entries only from the clinics
 * the requester is allowed to see.
 */
public record VisibleMedicalHistory(
    Long petId,
    List<Allergy> allergies,
    List<VaccinationRecord> vaccinations,
    List<MedicalHistoryEntry> entries) {
}
