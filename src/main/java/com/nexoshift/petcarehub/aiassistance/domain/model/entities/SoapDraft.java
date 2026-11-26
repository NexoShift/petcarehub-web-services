package com.nexoshift.petcarehub.aiassistance.domain.model.entities;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.InvalidAiAnalysisStateException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DocumentReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;

/**
 * SOAP draft written by the LLM. Lives inside the ClinicalAssistance aggregate:
 * it has no repository and is only created or changed through the aggregate root.
 */
@Entity
public class SoapDraft extends AuditableModel {

  private static final String WARNING_SEPARATOR = "\n";

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "clinical_assistance_id")
  private ClinicalAssistance clinicalAssistance;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String subjective;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String objective;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String assessment;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String plan;

  @Column(columnDefinition = "TEXT")
  private String warnings;

  @Column(length = 60)
  private String model;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DraftDecision decision;

  @Column(columnDefinition = "TEXT")
  private String editedText;

  @Embedded
  private DocumentReference report;

  protected SoapDraft() {
  }

  /** Public because the aggregate lives in another package; only the aggregate calls it. */
  public SoapDraft(ClinicalAssistance clinicalAssistance, String subjective, String objective,
      String assessment, String plan, List<String> warnings, String model) {
    this.clinicalAssistance = clinicalAssistance;
    this.subjective = requireText(subjective, "subjective");
    this.objective = requireText(objective, "objective");
    this.assessment = requireText(assessment, "assessment");
    this.plan = requireText(plan, "plan");
    this.warnings = warnings == null ? "" : String.join(WARNING_SEPARATOR, warnings);
    this.model = model;
    this.decision = DraftDecision.PENDING;
  }

  /** Only the aggregate root calls this method. */
  public void decide(DraftDecision newDecision, String newEditedText) {
    if (decision != DraftDecision.PENDING) {
      throw new InvalidAiAnalysisStateException("This draft was already " + decision);
    }
    this.decision = newDecision;
    this.editedText = newDecision == DraftDecision.EDITED ? newEditedText.trim() : null;
  }

  /** Only the aggregate root calls this method. */
  public void attachReport(DocumentReference newReport) {
    if (!decision.isConfirmed()) {
      throw new InvalidAiAnalysisStateException("Only confirmed drafts have a report");
    }
    this.report = newReport;
  }

  /** Final text: the edited one, or the SOAP sections as generated. */
  public String finalText() {
    return decision == DraftDecision.EDITED
        ? editedText
        : "S: %s%nO: %s%nA: %s%nP: %s".formatted(subjective, objective, assessment, plan);
  }

  public boolean isPending() {
    return decision == DraftDecision.PENDING;
  }

  public boolean hasReport() {
    return report != null && report.storageKey() != null;
  }

  public List<String> getWarnings() {
    return warnings == null || warnings.isBlank()
        ? List.of()
        : List.of(warnings.split(WARNING_SEPARATOR));
  }

  public String getSubjective() {
    return subjective;
  }

  public String getObjective() {
    return objective;
  }

  public String getAssessment() {
    return assessment;
  }

  public String getPlan() {
    return plan;
  }

  public String getModel() {
    return model;
  }

  public DraftDecision getDecision() {
    return decision;
  }

  public String getEditedText() {
    return editedText;
  }

  public DocumentReference getReport() {
    return hasReport() ? report : null;
  }

  private static String requireText(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("The draft " + field + " section is empty");
    }
    return value.trim();
  }
}
