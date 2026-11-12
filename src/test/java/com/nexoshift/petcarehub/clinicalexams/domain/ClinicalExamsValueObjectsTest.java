package com.nexoshift.petcarehub.clinicalexams.domain;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClinicalExamsValueObjectsTest {

  @Test
  void idsMustBePositive() {
    assertThat(new PetId(1L).petId()).isEqualTo(1L);
    assertThatThrownBy(() -> new PetId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetId(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VeterinarianId(-1L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalConsultationId(0L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fileTypeComesFromContentType() {
    assertThat(ClinicalFileType.fromContentType("image/jpeg")).isEqualTo(ClinicalFileType.IMAGE);
    assertThat(ClinicalFileType.fromContentType("video/mp4")).isEqualTo(ClinicalFileType.VIDEO);
    assertThat(ClinicalFileType.fromContentType("application/pdf"))
        .isEqualTo(ClinicalFileType.DOCUMENT);
    assertThatThrownBy(() -> ClinicalFileType.fromContentType("text/plain"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ClinicalFileType.fromContentType(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void storageLocationNeedsBucketAndKey() {
    assertThat(new StorageLocation("b", "k")).isEqualTo(new StorageLocation("b", "k"));
    assertThatThrownBy(() -> new StorageLocation(" ", "k"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new StorageLocation("b", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void cancellationNeedsReasonAndDate() {
    assertThat(new CancellationDetail(" x ", Instant.now()).reason()).isEqualTo("x");
    assertThatThrownBy(() -> new CancellationDetail(null, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CancellationDetail("x", null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
