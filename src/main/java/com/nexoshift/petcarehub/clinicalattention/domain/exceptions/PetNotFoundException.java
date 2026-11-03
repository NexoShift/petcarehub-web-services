package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class PetNotFoundException extends NotFoundException {

  public PetNotFoundException(Long petId) {
    super("Pet with id " + petId + " was not found");
  }
}
