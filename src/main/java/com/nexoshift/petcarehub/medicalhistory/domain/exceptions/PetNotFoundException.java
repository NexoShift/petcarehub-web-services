package com.nexoshift.petcarehub.medicalhistory.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class PetNotFoundException extends NotFoundException {

  public PetNotFoundException(Long petId) {
    super("Pet " + petId + " was not found");
  }
}
