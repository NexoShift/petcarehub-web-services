package com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates;

import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalFileNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.InvalidExamStateException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ClinicalFile;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ExamResult;
import com.nexoshift.petcarehub.clinicalexams.domain.model.events.ClinicalExamRequestedEvent;
import com.nexoshift.petcarehub.clinicalexams.domain.model.events.ExamResultValidatedEvent;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.CancellationDetail;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalFileType;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamType;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate Root of Clinical Exams: a complementary exam requested during a consultation.
 * It keeps its own life cycle, so it can continue after the consultation is closed.
 */
@Entity
public class ClinicalExam extends AuditableAbstractAggregateRoot<ClinicalExam> {

    @Embedded
    @AttributeOverride(name = "clinicalConsultationId",
            column = @Column(name = "consultation_id", nullable = false))
    private ClinicalConsultationId consultationId;

    @Embedded
    @AttributeOverride(name = "petId", column = @Column(name = "pet_id", nullable = false))
    private PetId petId;

    @Embedded
    @AttributeOverride(name = "veterinarianId",
            column = @Column(name = "requested_by", nullable = false))
    private VeterinarianId requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamType type;

    @Column(length = 500)
    private String indication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status;

    @Column(nullable = false)
    private Instant requestedAt;

    private Instant startedAt;

    private Instant completedAt;

    @Embedded
    @AttributeOverride(name = "reason", column = @Column(name = "cancellation_reason", length = 500))
    private CancellationDetail cancellation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exam_result_id")
    private ExamResult result;

    @OneToMany(mappedBy = "clinicalExam", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<ClinicalFile> files = new ArrayList<>();

    protected ClinicalExam() {
    }

    public ClinicalExam(ClinicalConsultationId consultationId, PetId petId,
                        VeterinarianId requestedBy, ExamType type, String indication) {
        if (consultationId == null || petId == null || requestedBy == null || type == null) {
            throw new IllegalArgumentException("Consultation, pet, veterinarian and type are required");
        }
        this.consultationId = consultationId;
        this.petId = petId;
        this.requestedBy = requestedBy;
        this.type = type;
        this.indication = indication == null || indication.isBlank() ? null : indication.trim();
        this.status = ExamStatus.REQUESTED;
        this.requestedAt = Instant.now();
    }

    public void notifyRequested() {
        registerEvent(new ClinicalExamRequestedEvent(
                getId(), getPetId(), getConsultationId(), requestedAt));
    }

    public void start() {
        ensureStatus(ExamStatus.REQUESTED, "Only requested exams can be started");
        this.status = ExamStatus.IN_PROCESS;
        this.startedAt = Instant.now();
    }

    public void registerResult(String summary, String values, String interpretation,
                               LocalDate resultDate) {
        ensureStatus(ExamStatus.IN_PROCESS, "Results can only be registered while in process");
        if (result != null) {
            throw new InvalidExamStateException("The exam already has a result");
        }
        this.result = new ExamResult(summary, values, interpretation, resultDate);
    }

    public void validateResult(VeterinarianId validator) {
        ensureStatus(ExamStatus.IN_PROCESS, "Results can only be validated while in process");
        if (result == null) {
            throw new InvalidExamStateException("The exam has no result to validate");
        }
        result.validate(validator);
        registerEvent(new ExamResultValidatedEvent(
                getId(), result.getId(), getPetId(), result.getValidatedAt()));
    }

    public void ensureCanAttachFile(String contentType) {
        if (status != ExamStatus.IN_PROCESS && status != ExamStatus.COMPLETED) {
            throw new InvalidExamStateException("Files can only be attached to started exams");
        }
        ClinicalFileType.fromContentType(contentType);
    }

    public ClinicalFile attachFile(String fileName, String contentType, long sizeBytes,
                                   StorageLocation storageLocation) {
        ensureCanAttachFile(contentType);
        var file = new ClinicalFile(this, fileName, contentType, sizeBytes, storageLocation);
        files.add(file);
        return file;
    }

    public void complete() {
        ensureStatus(ExamStatus.IN_PROCESS, "Only exams in process can be completed");
        if (result == null || !result.isValidated()) {
            throw new InvalidExamStateException("An exam can only be completed with a validated result");
        }
        this.status = ExamStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (status != ExamStatus.REQUESTED && status != ExamStatus.IN_PROCESS) {
            throw new InvalidExamStateException("Only requested or in process exams can be cancelled");
        }
        this.cancellation = new CancellationDetail(reason, Instant.now());
        this.status = ExamStatus.CANCELLED;
    }

    public ClinicalFile findFile(Long fileId) {
        return files.stream()
                .filter(file -> fileId.equals(file.getId()))
                .findFirst()
                .orElseThrow(() -> new ClinicalFileNotFoundException(fileId));
    }

    public Optional<ClinicalFile> findFileByStorageLocation(StorageLocation storageLocation) {
        return files.stream()
                .filter(file -> file.getStorageLocation().equals(storageLocation))
                .findFirst();
    }

    private void ensureStatus(ExamStatus expected, String message) {
        if (status != expected) {
            throw new InvalidExamStateException(message);
        }
    }

    public Long getConsultationId() {
        return consultationId.clinicalConsultationId();
    }

    public Long getPetId() {
        return petId.petId();
    }

    public Long getRequestedBy() {
        return requestedBy.veterinarianId();
    }

    public ExamType getType() {
        return type;
    }

    public String getIndication() {
        return indication;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Optional<CancellationDetail> getCancellation() {
        return Optional.ofNullable(cancellation);
    }

    public Optional<ExamResult> getResult() {
        return Optional.ofNullable(result);
    }

    public List<ClinicalFile> getFiles() {
        return List.copyOf(files);
    }
}