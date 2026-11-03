package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ClinicalConsultationNotFoundException extends NotFoundException {

  public ClinicalConsultationNotFoundException(Long consultationId) {
    super("Clinical consultation with id " + consultationId + " was not found");
  }
}
