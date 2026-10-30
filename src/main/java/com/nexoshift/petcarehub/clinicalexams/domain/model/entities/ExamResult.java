package com.nexoshift.petcarehub.clinicalexams.domain.model.entities;

import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.InvalidExamStateException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.time.LocalDate;

/** Result of an exam. It is never overwritten: once validated it is final. */
@Entity
public class ExamResult extends AuditableModel {

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(name = "result_values", length = 4000)
    private String values;

    @Column(length = 2000)
    private String interpretation;

    @Column(nullable = false)
    private LocalDate resultDate;

    @Column(nullable = false)
    private boolean validated;

    @Embedded
    @AttributeOverride(name = "veterinarianId", column = @Column(name = "validated_by"))
    private VeterinarianId validatedBy;

    private Instant validatedAt;

    protected ExamResult() {
    }

    public ExamResult(String summary, String values, String interpretation, LocalDate resultDate) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Result summary is required");
        }
        if (resultDate == null || resultDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Result date is required and cannot be in the future");
        }
        this.summary = summary.trim();
        this.values = values;
        this.interpretation = interpretation;
        this.resultDate = resultDate;
        this.validated = false;
    }

    public void validate(VeterinarianId validator) {
        if (validated) {
            throw new InvalidExamStateException("The exam result is already validated");
        }
        this.validated = true;
        this.validatedBy = validator;
        this.validatedAt = Instant.now();
    }

    public String getSummary() {
        return summary;
    }

    public String getValues() {
        return values;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public LocalDate getResultDate() {
        return resultDate;
    }

    public boolean isValidated() {
        return validated;
    }

    public Long getValidatedBy() {
        return validatedBy == null ? null : validatedBy.veterinarianId();
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }
}