package com.nexoshift.petcarehub.shared.domain.services;

import java.io.InputStream;
import java.time.Duration;

public interface FileStorageService {

    String upload(InputStream inputStream, long contentLength, String contentType, String folder);
    String generatePresignedUrl(String objectKey, Duration duration);
    void delete(String objectKey);
}
