package com.nexoshift.petcarehub.clinicalexams.infrastructure.storage.s3.configuration;

import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("prod")
public class S3FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorageServiceImpl(S3Client s3Client,
                                    @Value("${clinical-exams.storage.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public StorageLocation upload(byte[] content, String fileName, String contentType) {
        var key = "clinical-exams/" + UUID.randomUUID() + "-" + sanitize(fileName);
        var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
        return new StorageLocation(bucket, key);
    }

    @Override
    public byte[] download(StorageLocation storageLocation) {
        var request = GetObjectRequest.builder()
                .bucket(storageLocation.bucket())
                .key(storageLocation.key())
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    private String sanitize(String fileName) {
        return fileName == null ? "file" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}