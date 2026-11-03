package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage;

/** Port for every file of the module: vision images and report PDFs. Local today, S3 later. */
public interface FileStorageService {

  /**
   * Stores the content under a new key such as "reports/2026/11/uuid.pdf".
   *
   * @param folder logical folder: "vision" or "reports"
   */
  StoredFile store(byte[] content, String contentType, String folder);

  byte[] load(String storageKey);

  void delete(String storageKey);
}
