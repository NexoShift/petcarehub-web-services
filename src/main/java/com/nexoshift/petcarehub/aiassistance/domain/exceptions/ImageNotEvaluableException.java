package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.UnprocessableEntityException;

/** The file arrived but cannot be analyzed: not an image, too small, or a colored "radiograph". */
public class ImageNotEvaluableException extends UnprocessableEntityException {

  public ImageNotEvaluableException(String reason) {
    super("The image cannot be evaluated: " + reason);
  }
}
