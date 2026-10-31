package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class VisionAnalysisNotFoundException extends NotFoundException {

  public VisionAnalysisNotFoundException(Long id) {
    super("Vision analysis " + id + " was not found");
  }
}
