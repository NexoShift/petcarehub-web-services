package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ClinicalAssistanceNotFoundException extends NotFoundException {

  public ClinicalAssistanceNotFoundException(Long id) {
    super("Clinical assistance " + id + " was not found");
  }
}
