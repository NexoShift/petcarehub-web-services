package com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;

/** Outbound port to keep the content of clinical files outside the database. */
public interface FileStorageService {

    StorageLocation upload(byte[] content, String fileName, String contentType);

    byte[] download(StorageLocation storageLocation);
}