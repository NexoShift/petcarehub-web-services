package com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage;

import com.nexoshift.petcarehub.shared.domain.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Set;

/**
 * Service for storing profile images in Amazon S3
 */
@Service
public class ProfileImageStorageService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final FileStorageService fileStorageService;

    /**
     * Constructs the profile image storage service.
     *
     * @param fileStorageService the file storage service
     */
    public ProfileImageStorageService(FileStorageService fileStorageService){
        this.fileStorageService = fileStorageService;
    }

    public String upload(MultipartFile image, String folder){
        if(image == null) return null;
        if(image.isEmpty() || image.getSize() > MAX_IMAGE_SIZE){
            throw new IllegalArgumentException("Profile image must not be empty or exceed 5MB");
        }
        if(!ALLOWED_TYPES.contains(image.getContentType())){
            throw new IllegalArgumentException("Unsupported image type");
        }

        try {
            var inputStream = image.getInputStream();
            return fileStorageService.upload(inputStream,image.getSize(),image.getContentType(),folder);
        }catch (IOException e){
            throw new UncheckedIOException("Error reading the profile image", e);
        }
    }

    /**
     * Deletes a stored profile image
     *
     * @param objectKey the S3 object key
     */
    public void delete(String objectKey){
        if(objectKey != null && !objectKey.isBlank()){
            fileStorageService.delete(objectKey);
        }
    }

    /**
     * Generates a temporary URL for accessing a profile image.
     *
     * @param objectKey the stored S3 object key
     * @return the temporary image URL, or null if no image exists
     */
    public String generatePresignedUrl(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        return fileStorageService.generatePresignedUrl(
                objectKey,
                Duration.ofMinutes(15)
        );
    }
}
