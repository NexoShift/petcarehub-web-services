package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage;

/** Result of storing a file: a provider-independent key plus metadata. */
public record StoredFile(String storageKey, String contentType, long sizeBytes) {
}
