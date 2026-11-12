package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/** E-mail used to authenticate. Always stored in lower case. */
@Embeddable
public record EmailAddress(String address) {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  public EmailAddress {
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("Email address is required");
    }
    address = address.trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(address).matches()) {
      throw new IllegalArgumentException("Email address format is not valid");
    }
  }
}
