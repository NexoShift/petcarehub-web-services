package com.nexoshift.petcarehub.aiassistance.infrastructure.storage.s3;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.shared.domain.services.FileStorageServiceS3;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StoredFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** AI storage on S3: delegates to the shared S3 service. Enabled with ai.storage.provider=s3. */
@Service("aiS3FileStorageService")
@ConditionalOnProperty(name = "ai.storage.provider", havingValue = "s3")
public class S3FileStorageServiceImpl implements FileStorageService {

  private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(2);

  private final FileStorageServiceS3 s3;
  private final HttpClient http = HttpClient.newHttpClient();
  private static final Logger LOGGER = LoggerFactory.getLogger(S3FileStorageServiceImpl.class);


  public S3FileStorageServiceImpl(
          FileStorageServiceS3 s3) {
    this.s3 = s3;
    LOGGER.info("AI storage: S3");

  }

  @Override
  public StoredFile store(byte[] content, String contentType, String folder) {
    // shared builds and returns the key (e.g. "vision/uuid.jpg"): that is what we save
    var key = s3.upload(new ByteArrayInputStream(content), content.length, contentType, folder);
    return new StoredFile(key, contentType, content.length);
    // dentro del constructor:
  }

  @Override
  public byte[] load(String storageKey) {
    var url = s3.generatePresignedUrl(storageKey, DOWNLOAD_URL_TTL);
    try {
      var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
      var response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
      if (response.statusCode() != 200) {
        throw new IllegalStateException("S3 answered " + response.statusCode() + " for " + storageKey);
      }
      return response.body();
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(exception);
    }
  }

  @Override
  public void delete(String storageKey) {
    s3.delete(storageKey);
  }
}