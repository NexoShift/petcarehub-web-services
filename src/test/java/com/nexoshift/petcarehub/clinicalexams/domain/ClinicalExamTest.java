package com.nexoshift.petcarehub.clinicalexams.domain;

import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalFileNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.InvalidExamStateException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.events.ClinicalExamRequestedEvent;
import com.nexoshift.petcarehub.clinicalexams.domain.model.events.ExamResultValidatedEvent;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Ciclo de vida del examen: REQUESTED -> IN_PROCESS -> COMPLETED, o CANCELLED. */
class ClinicalExamTest {

  private static final StorageLocation LOCATION = new StorageLocation("bucket", "exams/1.png");

  private static ClinicalExam newExam() {
    return new ClinicalExam(new ClinicalConsultationId(10L), new ClinicId(1L), new PetId(20L),
        new VeterinarianId(30L), ExamType.RADIOGRAPHY, "  Tórax lateral  ");
  }

  private static ClinicalExam examInProcess() {
    var exam = newExam();
    exam.start();
    return exam;
  }

  private static ClinicalExam examWithValidatedResult() {
    var exam = examInProcess();
    exam.registerResult("Sin hallazgos", "{}", "Normal", LocalDate.now());
    exam.validateResult(new com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId(31L));
    return exam;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> events(ClinicalExam exam) {
    try {
      Method method = AbstractAggregateRoot.class.getDeclaredMethod("domainEvents");
      method.setAccessible(true);
      return List.copyOf((Collection<Object>) method.invoke(exam));
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void newExamStartsRequestedWithTrimmedIndication() {
    var exam = newExam();

    assertThat(exam.getStatus()).isEqualTo(ExamStatus.REQUESTED);
    assertThat(exam.getIndication()).isEqualTo("Tórax lateral");
    assertThat(exam.getConsultationId()).isEqualTo(10L);
    assertThat(exam.getPetId()).isEqualTo(20L);
    assertThat(exam.getClinicId()).isEqualTo(1L);
    assertThat(exam.getRequestedBy()).isEqualTo(30L);
    assertThat(exam.getRequestedAt()).isNotNull();
    assertThat(exam.getResult()).isEmpty();
    assertThat(exam.getCancellation()).isEmpty();
    assertThat(exam.getFiles()).isEmpty();
  }

  @Test
  void blankIndicationIsStoredAsNull() {
    var exam = new ClinicalExam(new ClinicalConsultationId(1L), new ClinicId(1L), new PetId(1L),
        new VeterinarianId(1L), ExamType.LABORATORY, "   ");

    assertThat(exam.getIndication()).isNull();
  }

  @Test
  void requiresConsultationClinicPetVeterinarianAndType() {
    assertThatThrownBy(() -> new ClinicalExam(new ClinicalConsultationId(1L), null, new PetId(1L),
        new VeterinarianId(1L), ExamType.OTHER, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalExam(null, new ClinicId(1L), new PetId(1L), new VeterinarianId(1L),
        ExamType.OTHER, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalExam(new ClinicalConsultationId(1L), new ClinicId(1L), null,
        new VeterinarianId(1L), ExamType.OTHER, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalExam(new ClinicalConsultationId(1L), new ClinicId(1L), new PetId(1L),
        null, ExamType.OTHER, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalExam(new ClinicalConsultationId(1L), new ClinicId(1L), new PetId(1L),
        new VeterinarianId(1L), null, null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void notifyRequestedRegistersEvent() {
    var exam = newExam();

    exam.notifyRequested();

    assertThat(events(exam)).singleElement().isInstanceOfSatisfying(
        ClinicalExamRequestedEvent.class, event -> {
          assertThat(event.petId()).isEqualTo(20L);
          assertThat(event.consultationId()).isEqualTo(10L);
          assertThat(event.requestedAt()).isEqualTo(exam.getRequestedAt());
        });
  }

  @Test
  void startMovesToInProcessOnlyOnce() {
    var exam = examInProcess();

    assertThat(exam.getStatus()).isEqualTo(ExamStatus.IN_PROCESS);
    assertThat(exam.getStartedAt()).isNotNull();
    assertThatThrownBy(exam::start).isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void resultCanOnlyBeRegisteredWhileInProcessAndOnce() {
    assertThatThrownBy(() -> newExam().registerResult("R", null, null, LocalDate.now()))
        .isInstanceOf(InvalidExamStateException.class);

    var exam = examInProcess();
    exam.registerResult("  Fractura  ", "v", "i", LocalDate.now());

    assertThat(exam.getResult()).get().satisfies(result -> {
      assertThat(result.getSummary()).isEqualTo("Fractura");
      assertThat(result.isValidated()).isFalse();
    });
    assertThatThrownBy(() -> exam.registerResult("Otro", null, null, LocalDate.now()))
        .isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void resultNeedsSummaryAndDateNotInFuture() {
    var exam = examInProcess();

    assertThatThrownBy(() -> exam.registerResult(" ", null, null, LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> exam.registerResult("R", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> exam.registerResult("R", null, null, LocalDate.now().plusDays(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateResultRequiresResultAndRegistersEvent() {
    var exam = examInProcess();
    assertThatThrownBy(() -> exam.validateResult(new VeterinarianId(31L)))
        .isInstanceOf(InvalidExamStateException.class);

    exam.registerResult("R", null, null, LocalDate.now());
    exam.validateResult(new VeterinarianId(31L));

    assertThat(exam.getResult()).get().satisfies(result -> {
      assertThat(result.isValidated()).isTrue();
      assertThat(result.getValidatedBy()).isEqualTo(31L);
      assertThat(result.getValidatedAt()).isNotNull();
    });
    assertThat(events(exam)).singleElement().isInstanceOf(ExamResultValidatedEvent.class);
    assertThatThrownBy(() -> exam.validateResult(new VeterinarianId(31L)))
        .isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void validateResultFailsWhenNotInProcess() {
    assertThatThrownBy(() -> newExam().validateResult(new VeterinarianId(1L)))
        .isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void completeRequiresValidatedResult() {
    var withoutResult = examInProcess();
    assertThatThrownBy(withoutResult::complete).isInstanceOf(InvalidExamStateException.class);

    var notValidated = examInProcess();
    notValidated.registerResult("R", null, null, LocalDate.now());
    assertThatThrownBy(notValidated::complete).isInstanceOf(InvalidExamStateException.class);

    var exam = examWithValidatedResult();
    exam.complete();
    assertThat(exam.getStatus()).isEqualTo(ExamStatus.COMPLETED);
    assertThat(exam.getCompletedAt()).isNotNull();
    assertThatThrownBy(exam::complete).isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void cancelFromRequestedOrInProcessWithReason() {
    var requested = newExam();
    requested.cancel("  El dueño no vino  ");
    assertThat(requested.getStatus()).isEqualTo(ExamStatus.CANCELLED);
    assertThat(requested.getCancellation()).get().satisfies(c -> {
      assertThat(c.reason()).isEqualTo("El dueño no vino");
      assertThat(c.cancelledAt()).isNotNull();
    });

    var inProcess = examInProcess();
    inProcess.cancel("Equipo dañado");
    assertThat(inProcess.getStatus()).isEqualTo(ExamStatus.CANCELLED);

    assertThatThrownBy(() -> newExam().cancel(" ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void completedOrCancelledExamCannotBeCancelled() {
    var completed = examWithValidatedResult();
    completed.complete();
    assertThatThrownBy(() -> completed.cancel("x")).isInstanceOf(InvalidExamStateException.class);

    var cancelled = newExam();
    cancelled.cancel("x");
    assertThatThrownBy(() -> cancelled.cancel("y")).isInstanceOf(InvalidExamStateException.class);
    assertThatThrownBy(cancelled::start).isInstanceOf(InvalidExamStateException.class);
  }

  @Test
  void filesOnlyForStartedOrCompletedExams() {
    assertThatThrownBy(() -> newExam().attachFile("a.png", "image/png", 10, LOCATION))
        .isInstanceOf(InvalidExamStateException.class);

    var cancelled = examInProcess();
    cancelled.cancel("x");
    assertThatThrownBy(() -> cancelled.ensureCanAttachFile("image/png"))
        .isInstanceOf(InvalidExamStateException.class);

    var inProcess = examInProcess();
    var file = inProcess.attachFile("a.png", "image/png", 10, LOCATION);
    assertThat(file.getType()).isEqualTo(ClinicalFileType.IMAGE);
    assertThat(inProcess.getFiles()).hasSize(1);

    var completed = examWithValidatedResult();
    completed.complete();
    completed.attachFile("informe.pdf", "application/pdf", 10, LOCATION);
    assertThat(completed.getFiles()).hasSize(1);
  }

  @Test
  void attachFileValidatesTypeNameAndSize() {
    var exam = examInProcess();

    assertThatThrownBy(() -> exam.attachFile("a.exe", "application/octet-stream", 10, LOCATION))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> exam.attachFile(" ", "image/png", 10, LOCATION))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> exam.attachFile("a.png", "image/png", 0, LOCATION))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(exam.getFiles()).isEmpty();
  }

  @Test
  void filesListIsReadOnlyAndSearchable() {
    var exam = examInProcess();
    var other = new StorageLocation("bucket", "exams/2.mp4");
    exam.attachFile("a.png", "image/png", 10, LOCATION);
    exam.attachFile("b.mp4", "video/mp4", 10, other);

    assertThatThrownBy(() -> exam.getFiles().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(exam.findFileByStorageLocation(other)).get()
        .extracting(f -> f.getType()).isEqualTo(ClinicalFileType.VIDEO);
    assertThat(exam.findFileByStorageLocation(new StorageLocation("b", "k"))).isEmpty();
    assertThatThrownBy(() -> exam.findFile(99L))
        .isInstanceOf(ClinicalFileNotFoundException.class);
  }
}
