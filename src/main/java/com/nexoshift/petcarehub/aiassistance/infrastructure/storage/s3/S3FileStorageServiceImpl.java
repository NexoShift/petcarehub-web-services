package com.nexoshift.petcarehub.aiassistance.infrastructure.storage.s3;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StoredFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Amazon S3 implementation, enabled with ai.storage.provider=s3. Fabrizio's S3 work goes
 * here (option A: delegate to a shared ObjectStorage). Keys come from StorageKeys, the
 * same as in local storage, so stored records do not change.
 */
@Service("aiS3FileStorageService")
@ConditionalOnProperty(name = "ai.storage.provider", havingValue = "s3")
public class S3FileStorageServiceImpl implements FileStorageService {

  @Override
  public StoredFile store(byte[] content, String contentType, String folder) {
    throw new UnsupportedOperationException("S3 storage is not configured yet");
  }

  @Override
  public byte[] load(String storageKey) {
    throw new UnsupportedOperationException("S3 storage is not configured yet");
  }

  @Override
  public void delete(String storageKey) {
    throw new UnsupportedOperationException("S3 storage is not configured yet");
  }
}
