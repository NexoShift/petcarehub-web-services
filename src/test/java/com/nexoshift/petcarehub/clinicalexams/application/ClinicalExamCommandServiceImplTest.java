package com.nexoshift.petcarehub.clinicalexams.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexoshift.petcarehub.clinicalexams.application.internal.access.ClinicalExamAccessGuard;
import com.nexoshift.petcarehub.clinicalexams.application.internal.commandservices.ClinicalExamCommandServiceImpl;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ConsultationNotAvailableException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.InvalidExamStateException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.RequestClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.StartClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.UploadClinicalFileCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamType;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalexams.domain.repositories.ClinicalExamRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClinicalExamCommandServiceImplTest {

  private ClinicalExamRepository repository;
  private ExternalClinicalAttentionService clinicalAttention;
  private ClinicalExamAccessGuard guard;
  private FileStorageService storage;
  private ClinicalExamCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(ClinicalExamRepository.class);
    clinicalAttention = mock(ExternalClinicalAttentionService.class);
    guard = mock(ClinicalExamAccessGuard.class);
    storage = mock(FileStorageService.class);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service = new ClinicalExamCommandServiceImpl(repository, clinicalAttention, guard, storage);
  }

  private static ClinicalExam exam() {
    return new ClinicalExam(new ClinicalConsultationId(5L), new ClinicId(1L), new PetId(7L), new VeterinarianId(100L),
        ExamType.LABORATORY, null);
  }

  @Test
  void requestTakesPetAndClinicFromTheConsultationAndTheVetFromTheUser() {
    when(guard.requireVeterinarianOfConsultation(10L, 5L)).thenReturn(new VeterinarianId(100L));
    when(clinicalAttention.isConsultationInProgress(5L)).thenReturn(true);
    when(clinicalAttention.fetchPetIdByConsultationId(5L)).thenReturn(Optional.of(7L));
    when(clinicalAttention.fetchClinicIdByConsultationId(5L)).thenReturn(Optional.of(1L));

    var exam = service.handle(new RequestClinicalExamCommand(5L, 10L, ExamType.LABORATORY, "Hemograma"));

    assertThat(exam.getPetId()).isEqualTo(7L);
    assertThat(exam.getRequestedBy()).isEqualTo(100L);
    assertThat(exam.getClinicId()).isEqualTo(1L);
    assertThat(exam.getStatus()).isEqualTo(ExamStatus.REQUESTED);
  }

  @Test
  void requestNeedsAConsultationInProgress() {
    when(guard.requireVeterinarianOfConsultation(10L, 5L)).thenReturn(new VeterinarianId(100L));
    when(clinicalAttention.isConsultationInProgress(5L)).thenReturn(false);

    assertThatThrownBy(() -> service.handle(
        new RequestClinicalExamCommand(5L, 10L, ExamType.LABORATORY, null)))
        .isInstanceOf(ConsultationNotAvailableException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void unknownExamIsNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.handle(new StartClinicalExamCommand(1L, 10L)))
        .isInstanceOf(ClinicalExamNotFoundException.class);
  }

  @Test
  void aFileIsNotStoredWhenTheExamDoesNotAcceptFiles() {
    when(repository.findById(1L)).thenReturn(Optional.of(exam()));

    assertThatThrownBy(() -> service.handle(
        new UploadClinicalFileCommand(1L, 10L, "a.png", "image/png", new byte[] {1})))
        .isInstanceOf(InvalidExamStateException.class);
    verify(storage, never()).upload(any(), anyString(), anyString());
  }

  @Test
  void uploadStoresTheContentAndKeepsOnlyTheLocation() {
    var exam = exam();
    exam.start();
    when(repository.findById(1L)).thenReturn(Optional.of(exam));
    var location = new StorageLocation("local", "clinical-exams/x.png");
    when(storage.upload(any(), anyString(), anyString())).thenReturn(location);

    var file = service.handle(new UploadClinicalFileCommand(1L, 10L, "x.png", "image/png", new byte[] {1, 2}));

    assertThat(file.getStorageLocation()).isEqualTo(location);
    assertThat(file.getSizeBytes()).isEqualTo(2L);
    verify(guard).requireCanWrite(10L, exam);
  }
}
