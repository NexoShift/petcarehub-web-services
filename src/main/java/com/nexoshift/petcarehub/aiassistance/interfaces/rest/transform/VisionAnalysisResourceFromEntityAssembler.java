package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.entities.VisionFinding;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.VisionAnalysisResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.VisionFindingResource;

public final class VisionAnalysisResourceFromEntityAssembler {

  private static final String BASE_PATH = "/api/v1/ai/vision-analyses/";

  private VisionAnalysisResourceFromEntityAssembler() {
  }

  public static VisionAnalysisResource toResourceFromEntity(VisionAnalysis entity) {
    var findings = entity.getFindings().stream()
        .map(VisionAnalysisResourceFromEntityAssembler::toFindingResource)
        .toList();
    return new VisionAnalysisResource(entity.getId(), entity.getPetId(),
        entity.getConsultationId(), entity.getImageType().name(), entity.getStatus().name(),
        entity.getModelId(), entity.getFailureReason(), entity.getRequesterRole().name(),
        BASE_PATH + entity.getId() + "/image", findings, entity.getCreatedAt());
  }

  private static VisionFindingResource toFindingResource(VisionFinding finding) {
    var box = finding.getBoundingBox();
    return new VisionFindingResource(finding.getId(), finding.getLabel(),
        finding.getConfidence().asPercentage(), finding.isProbable(), finding.hasLocation(),
        box == null ? null : box.left(), box == null ? null : box.top(),
        box == null ? null : box.width(), box == null ? null : box.height());
  }
}
