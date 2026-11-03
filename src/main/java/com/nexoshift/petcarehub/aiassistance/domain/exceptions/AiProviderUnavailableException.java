package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ServiceUnavailableException;

/** The LLM or vision provider did not answer in time or answered with an error. */
public class AiProviderUnavailableException extends ServiceUnavailableException {

  public AiProviderUnavailableException(String provider, Throwable cause) {
    super("The AI provider " + provider + " is not available right now. "
        + "You can continue the consultation without assistance.", cause);
  }
}
