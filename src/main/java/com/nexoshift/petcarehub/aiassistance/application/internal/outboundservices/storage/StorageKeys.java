package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/** The only place that builds storage keys, so local and S3 use exactly the same ones. */
public final class StorageKeys {

  private static final Map<String, String> EXTENSIONS = Map.of(
      "image/jpeg", "jpg", "image/png", "png", "application/pdf", "pdf");

  private StorageKeys() {
  }

  public static String newKey(String folder, String contentType) {
    var now = LocalDate.now(ZoneId.of("America/Lima"));
    var extension = EXTENSIONS.getOrDefault(contentType, "bin");
    return "%s/%d/%02d/%s.%s".formatted(
        folder, now.getYear(), now.getMonthValue(), UUID.randomUUID(), extension);
  }
}
