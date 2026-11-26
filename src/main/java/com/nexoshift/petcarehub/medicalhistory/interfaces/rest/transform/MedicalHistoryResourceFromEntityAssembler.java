package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.transform;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.VisibleMedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.Allergy;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.MedicalHistoryEntry;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.VaccinationRecord;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.AllergyResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.MedicalHistoryEntryResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.MedicalHistoryResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.VaccinationResource;

public final class MedicalHistoryResourceFromEntityAssembler {

  private MedicalHistoryResourceFromEntityAssembler() {
  }

  public static MedicalHistoryResource toResourceFromEntity(VisibleMedicalHistory history) {
    return new MedicalHistoryResource(history.petId(),
        history.allergies().stream()
            .map(MedicalHistoryResourceFromEntityAssembler::toResource).toList(),
        history.vaccinations().stream()
            .map(MedicalHistoryResourceFromEntityAssembler::toResource).toList(),
        history.entries().stream()
            .map(MedicalHistoryResourceFromEntityAssembler::toResource).toList());
  }

  private static AllergyResource toResource(Allergy allergy) {
    return new AllergyResource(allergy.getId(), allergy.getAllergen(), allergy.getReaction(),
        allergy.getSeverity(), allergy.getNotes(), allergy.getReportedBy(),
        allergy.getRecordedAt());
  }

  private static VaccinationResource toResource(VaccinationRecord vaccination) {
    return new VaccinationResource(vaccination.getId(), vaccination.getVaccineName(),
        vaccination.getDoseNumber(), vaccination.getAppliedOn(), vaccination.getNextDoseOn(),
        vaccination.getBatchNumber(), vaccination.getNotes(), vaccination.getVeterinarianId(),
        vaccination.getClinicId());
  }

  private static MedicalHistoryEntryResource toResource(MedicalHistoryEntry entry) {
    return new MedicalHistoryEntryResource(entry.getId(), entry.getType(),
        entry.getReferenceId(), entry.getConsultationId(), entry.getClinicId(),
        entry.getVeterinarianId(), entry.getExamType(), entry.getSummary(),
        entry.getOccurredAt());
  }
}
