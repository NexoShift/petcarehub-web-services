package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import java.util.List;

/** Validation helpers shared by the commands of this package. */
final class CommandValidation {

  private CommandValidation() {
  }

  static Long requiredId(Long value, String field) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(field + " must be greater than zero");
    }
    return value;
  }

  static Long optionalId(Long value, String field) {
    return value == null ? null : requiredId(value, field);
  }

  static <T> T requiredValue(T value, String field) {
    if (value == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
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
      throw new IllegalArgumentException(
          field + " must not exceed " + maxLength + " characters");
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

  /** Trims every item, drops blank and repeated ones and keeps the original order. */
  static List<String> textList(List<String> values, int maxItems, int maxLength, String field) {
    if (values == null) {
      return List.of();
    }
    var items = values.stream()
        .map(value -> optionalText(value, maxLength, field))
        .filter(item -> item != null)
        .distinct()
        .toList();
    if (items.size() > maxItems) {
      throw new IllegalArgumentException(field + " must not have more than " + maxItems + " items");
    }
    return items;
  }
}
