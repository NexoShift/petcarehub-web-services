package com.nexoshift.petcarehub.shared.infrastructure.storage.s3.services;

import com.nexoshift.petcarehub.shared.domain.services.FileStorageServiceS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3FileStorageService implements FileStorageServiceS3 {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3FileStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucket){
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;

        System.out.println("S3 Bucket: " + bucket);
        System.out.println("S3 Client Region: " +
                s3Client.serviceClientConfiguration().region());
    }

    @Override
    public String upload(InputStream inputStream, long contentLength, String contentType, String folder) {
        String extension = switch (contentType){
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> throw new IllegalArgumentException("Unsupported content type");
        };
        if(folder == null || !folder.matches("[a-z0-9-]+(/[a-z0-9-]+)*")){
            throw new IllegalArgumentException("Invalid folder");
        }
        if(contentLength <= 0){
            throw new IllegalArgumentException("Empty file");
        }

        String objectKey = folder + "/" + UUID.randomUUID() + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket).key(objectKey).contentType(contentType).build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream,contentLength));

        return objectKey;
    }

    @Override
    public String generatePresignedUrl(String objectKey, Duration duration) {

        if(duration == null || duration.isNegative() || duration.isZero()){
            throw new IllegalArgumentException("Invalid URL duration");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest
                .builder()
                .signatureDuration(duration)
                .getObjectRequest(request)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String objectKey) {

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }
}
