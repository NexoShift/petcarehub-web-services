package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.UnprocessableEntityException;

/**
 * NUEVO. La mascota existe, pero no está registrada como paciente de la clínica (HTTP 422).
 * Primero hay que registrarla con POST /api/v1/clinics/{clinicId}/patients (Profiles).
 */
public class PetNotPatientOfClinicException extends UnprocessableEntityException {

  public PetNotPatientOfClinicException(Long petId, Long clinicId) {
    super("Pet " + petId + " is not a patient of clinic " + clinicId
        + ". Register it as a patient of the clinic first");
  }
}
