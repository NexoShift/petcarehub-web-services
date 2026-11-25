package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Medication;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Prescription;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.MedicationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.PrescriptionResource;

public final class PrescriptionResourceFromEntityAssembler {

  private PrescriptionResourceFromEntityAssembler() {
  }

  public static PrescriptionResource toResourceFromEntity(Prescription prescription) {
    return new PrescriptionResource(prescription.getId(), prescription.getIssuedAt(),
        prescription.getGeneralInstruction(),
        prescription.getMedications().stream()
            .map(PrescriptionResourceFromEntityAssembler::toResource).toList(),
        prescription.getPrescribedBy().veterinarianId());
  }

  private static MedicationResource toResource(Medication medication) {
    return new MedicationResource(medication.getId(), medication.getName(),
        medication.getDosage(), medication.getFrequency(), medication.getDuration(),
        medication.getIndication());
  }
}
