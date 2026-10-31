package com.nexoshift.petcarehub.aiassistance.domain.model.aggregates;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.InvalidAiAnalysisStateException;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.entities.VisionFinding;
import com.nexoshift.petcarehub.aiassistance.domain.model.events.VisionAnalysisCompletedEvent;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.AiAnalysisStatus;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BoundingBox;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ConfidenceScore;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Analysis of one image with computer vision. Holds where the image is stored, its status
 * and the findings the model returned with enough confidence.
 */
@Entity
public class VisionAnalysis extends AuditableAbstractAggregateRoot<VisionAnalysis> {

  private static final ZoneId LIMA = ZoneId.of("America/Lima");

  /** Optional: pet owners upload outside a consultation. */
  @Embedded
  private ClinicalConsultationId consultationId;

  @Embedded
  private PetId petId;

  @Embedded
  private RequesterId requesterId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RequesterRole requesterRole;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ImageType imageType;

  @Embedded
  private ImageReference image;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AiAnalysisStatus status;

  @Column(length = 120)
  private String modelId;

  @Column(length = 500)
  private String failureReason;

  // EAGER because open-in-view is off and the controller reads the findings after the service.
  @OneToMany(mappedBy = "visionAnalysis", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  @Fetch(FetchMode.SUBSELECT)
  @OrderBy("id ASC")
  private List<VisionFinding> findings = new ArrayList<>();

  protected VisionAnalysis() {
  }

  public VisionAnalysis(RequestVisionAnalysisCommand command, ImageReference image) {
    if (image == null) {
      throw new IllegalArgumentException("Image reference is required");
    }
    this.consultationId = command.consultationId() == null
        ? null : new ClinicalConsultationId(command.consultationId());
    this.petId = new PetId(command.petId());
    this.requesterId = new RequesterId(command.requesterId());
    this.requesterRole = command.requesterRole();
    this.imageType = command.imageType();
    this.image = image;
    this.status = AiAnalysisStatus.PENDING;
  }

  public void startProcessing() {
    if (status != AiAnalysisStatus.PENDING) {
      throw new InvalidAiAnalysisStateException("The analysis was already processed");
    }
    this.status = AiAnalysisStatus.PROCESSING;
  }

  /**
   * Keeps the finding only if the model is confident enough. Skin findings need a box;
   * radiograph findings never have one. Returns whether it was kept.
   */
  public boolean recordFinding(String label, ConfidenceScore confidence, BoundingBox box,
      double threshold) {
    if (status != AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("The analysis is not processing");
    }
    if (!confidence.isAtLeast(threshold)) {
      return false;
    }
    if (imageType == ImageType.SKIN && box == null) {
      throw new IllegalArgumentException("Skin findings need a location");
    }
    var location = imageType == ImageType.RADIOGRAPH ? null : box;
    findings.add(new VisionFinding(this, label, confidence, location));
    return true;
  }

  /** Completes the analysis, with or without findings. */
  public void complete(String usedModelId) {
    if (status != AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("The analysis is not processing");
    }
    this.modelId = usedModelId;
    this.status = AiAnalysisStatus.COMPLETED;
    registerEvent(new VisionAnalysisCompletedEvent(getId(), petId.petId(), findings.size()));
  }

  public void fail(String reason) {
    if (status != AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("The analysis is not processing");
    }
    this.status = AiAnalysisStatus.FAILED;
    this.failureReason = reason == null || reason.length() <= 500 ? reason : reason.substring(0, 500);
  }

  public boolean isCompleted() {
    return status == AiAnalysisStatus.COMPLETED;
  }

  /**
   * Text used when the veterinarian selects this analysis as a background for the LLM.
   * Only probable findings (50 % or more) are sent.
   */
  public String toBackgroundText() {
    var probable = getFindings().stream().filter(VisionFinding::isProbable).toList();
    var detail = probable.isEmpty()
        ? "sin hallazgos con confianza suficiente"
        : probable.stream()
            .map(finding -> finding.getLabel() + " (" + finding.getConfidence().asPercentage() + " %)")
            .collect(Collectors.joining(", "));
    var kind = imageType == ImageType.RADIOGRAPH ? "Análisis de radiografía" : "Análisis visual de piel";
    return "%s del %s: %s. Resultado orientativo de un modelo de IA."
        .formatted(kind, analysisDate(), detail);
  }

  public LocalDate analysisDate() {
    return getCreatedAt() == null ? LocalDate.now(LIMA) : getCreatedAt().atZone(LIMA).toLocalDate();
  }

  /** Short summary for the backgrounds list, for example "dermatitis 82 %". */
  public String findingsSummary() {
    var probable = getFindings().stream().filter(VisionFinding::isProbable).toList();
    return probable.isEmpty()
        ? "Sin hallazgos con confianza suficiente"
        : probable.stream()
            .map(finding -> finding.getLabel() + " " + finding.getConfidence().asPercentage() + " %")
            .collect(Collectors.joining(", "));
  }

  /** Most confident first. */
  public List<VisionFinding> getFindings() {
    return findings.stream()
        .sorted(Comparator.comparingDouble(
            (VisionFinding finding) -> finding.getConfidence().confidence()).reversed())
        .toList();
  }

  public Long getConsultationId() {
    return consultationId == null ? null : consultationId.consultationId();
  }

  public Long getPetId() {
    return petId.petId();
  }

  public Long getRequesterId() {
    return requesterId.requesterId();
  }

  public RequesterRole getRequesterRole() {
    return requesterRole;
  }

  public ImageType getImageType() {
    return imageType;
  }

  public ImageReference getImage() {
    return image;
  }

  public AiAnalysisStatus getStatus() {
    return status;
  }

  public String getModelId() {
    return modelId;
  }

  public String getFailureReason() {
    return failureReason;
  }
}
