package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class SoapDraftNotFoundException extends NotFoundException {

  public SoapDraftNotFoundException(Long draftId) {
    super("Draft " + draftId + " was not found");
  }
}
