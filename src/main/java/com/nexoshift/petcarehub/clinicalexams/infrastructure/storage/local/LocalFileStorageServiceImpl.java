package com.nexoshift.petcarehub.clinicalexams.infrastructure.storage.local;

import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Stores clinical files in a local folder. Used in development. */
@Service
@Profile("dev")
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final String BUCKET = "local";

    private final Path baseDirectory;

    public LocalFileStorageServiceImpl(
            @Value("${clinical-exams.storage.local.directory:uploads}") String baseDirectory) {
        this.baseDirectory = Path.of(baseDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StorageLocation upload(byte[] content, String fileName, String contentType) {
        var key = "clinical-exams/" + UUID.randomUUID() + "-" + sanitize(fileName);
        try {
            var target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return new StorageLocation(BUCKET, key);
        } catch (IOException exception) {
            throw new UncheckedIOException("The clinical file could not be stored", exception);
        }
    }

    @Override
    public byte[] download(StorageLocation storageLocation) {
        try {
            return Files.readAllBytes(resolve(storageLocation.key()));
        } catch (IOException exception) {
            throw new UncheckedIOException("The clinical file could not be read", exception);
        }
    }

    private Path resolve(String key) {
        var path = baseDirectory.resolve(key).normalize();
        if (!path.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }

    private String sanitize(String fileName) {
        return fileName == null ? "file" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}