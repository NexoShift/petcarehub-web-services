package com.nexoshift.petcarehub.clinicalexams.domain.model.entities;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalFileType;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.StorageLocation;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

/** Image, video or document of an exam. Its content lives in the file storage. */
@Entity
public class ClinicalFile extends AuditableModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinical_exam_id", nullable = false)
    private ClinicalExam clinicalExam;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClinicalFileType type;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Embedded
    @AttributeOverride(name = "bucket", column = @Column(name = "storage_bucket", nullable = false))
    @AttributeOverride(name = "key",
            column = @Column(name = "storage_key", nullable = false, length = 500))
    private StorageLocation storageLocation;

    @Column(nullable = false)
    private Instant uploadedAt;

    protected ClinicalFile() {
    }

    public ClinicalFile(ClinicalExam clinicalExam, String fileName, String contentType,
                        long sizeBytes, StorageLocation storageLocation) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("File must not be empty");
        }
        this.clinicalExam = clinicalExam;
        this.fileName = fileName;
        this.type = ClinicalFileType.fromContentType(contentType);
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageLocation = storageLocation;
        this.uploadedAt = Instant.now();
    }

    public String getFileName() {
        return fileName;
    }

    public ClinicalFileType getType() {
        return type;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}