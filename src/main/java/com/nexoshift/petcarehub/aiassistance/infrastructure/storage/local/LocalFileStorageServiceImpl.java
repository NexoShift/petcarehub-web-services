package com.nexoshift.petcarehub.aiassistance.infrastructure.storage.local;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StorageKeys;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StoredFile;
import com.nexoshift.petcarehub.aiassistance.infrastructure.storage.configuration.StorageProperties;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Stores files on disk under ai.storage.local-dir, with the same keys S3 will use. */
@Service("aiLocalFileStorageService")
@ConditionalOnProperty(name = "ai.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

  private final Path root;

  public LocalFileStorageServiceImpl(StorageProperties properties) throws IOException {
    this.root = Path.of(properties.localDir()).toAbsolutePath().normalize();
    Files.createDirectories(root);
  }

  @Override
  public StoredFile store(byte[] content, String contentType, String folder) {
    var key = StorageKeys.newKey(folder, contentType);
    var target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      Files.write(target, content);
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not store file " + key, exception);
    }
    return new StoredFile(key, contentType, content.length);
  }

  @Override
  public byte[] load(String storageKey) {
    try {
      return Files.readAllBytes(resolve(storageKey));
    } catch (NoSuchFileException exception) {
      throw new NotFoundException("File " + storageKey + " was not found");
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not read file " + storageKey, exception);
    }
  }

  @Override
  public void delete(String storageKey) {
    try {
      Files.deleteIfExists(resolve(storageKey));
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not delete file " + storageKey, exception);
    }
  }

  /** Prevents keys like "../../application.properties" from leaving the storage folder. */
  private Path resolve(String storageKey) {
    var path = root.resolve(storageKey).normalize();
    if (!path.startsWith(root)) {
      throw new IllegalArgumentException("Invalid storage key");
    }
    return path;
  }
}
