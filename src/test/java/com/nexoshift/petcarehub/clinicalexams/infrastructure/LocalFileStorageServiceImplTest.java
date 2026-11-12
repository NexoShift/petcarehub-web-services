package com.nexoshift.petcarehub.clinicalexams.infrastructure;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import com.nexoshift.petcarehub.clinicalexams.infrastructure.storage.local.LocalFileStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Almacenamiento local de archivos clínicos (perfil dev). */
class LocalFileStorageServiceImplTest {

  @TempDir
  Path folder;

  @Test
  void uploadThenDownloadReturnsSameBytes() {
    var storage = new LocalFileStorageServiceImpl(folder.toString());
    byte[] content = {1, 2, 3};

    var location = storage.upload(content, "placa tórax.png", "image/png");

    assertThat(location.bucket()).isEqualTo("local");
    assertThat(location.key()).startsWith("clinical-exams/").endsWith("placa_t_rax.png");
    assertThat(storage.download(location)).containsExactly(1, 2, 3);
  }

  @Test
  void rejectsKeysOutsideTheFolder() {
    var storage = new LocalFileStorageServiceImpl(folder.toString());

    assertThatThrownBy(() -> storage.download(new StorageLocation("local", "../../etc/passwd")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
