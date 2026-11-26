package com.nexoshift.petcarehub.medicalhistory.domain.model.commands;

/** Validation helpers shared by the commands of this package. */
final class TextValidation {

  private TextValidation() {
  }

  static void requiredId(Long value, String field) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(field + " must be greater than zero");
    }
  }

  /** Trims the text. Blank text becomes null. */
  static String optionalText(String value, int maxLength, String field) {
    if (value == null) {
      return null;
    }
    var text = value.trim();
    if (text.isEmpty()) {
      return null;
    }
    if (text.length() > maxLength) {
      throw new IllegalArgumentException(field + " must not exceed " + maxLength + " characters");
    }
    return text;
  }

  static String requiredText(String value, int maxLength, String field) {
    var text = optionalText(value, maxLength, field);
    if (text == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return text;
  }
}
