package com.nexoshift.petcarehub.aiassistance.interfaces.rest.controllers;

import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.services.VisionAnalysisQueryService;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.AvailableBackgroundResource;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backgrounds the web shows as checkboxes: previous consultations and completed vision
 * analyses of the pet, newest first. Technical debt: a controller should not call an ACL
 * port directly; move it to a ClinicalBackgroundQueryService if time allows.
 */
@RestController
@RequestMapping(value = "/api/v1/ai/clinical-backgrounds",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "AI Clinical Assistance")
@PreAuthorize("hasRole('VETERINARIAN')")
public class ClinicalBackgroundsController {

  private final ExternalClinicalAttentionService clinicalAttentionService;
  private final VisionAnalysisQueryService visionAnalysisQueryService;
  private final PetAccessGuard petAccessGuard;

  public ClinicalBackgroundsController(ExternalClinicalAttentionService clinicalAttentionService,
      VisionAnalysisQueryService visionAnalysisQueryService, PetAccessGuard petAccessGuard) {
    this.clinicalAttentionService = clinicalAttentionService;
    this.visionAnalysisQueryService = visionAnalysisQueryService;
    this.petAccessGuard = petAccessGuard;
  }

  @GetMapping
  @Operation(summary = "List the backgrounds of a pet that can be sent to the assistant")
  public List<AvailableBackgroundResource> getAvailableBackgrounds(@RequestParam Long petId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    petAccessGuard.ensureCanAccessPet(petId, currentUser.getId(), RequesterRole.VETERINARIAN);
    var consultations = clinicalAttentionService.fetchConsultationsByPetId(petId).stream()
        .map(consultation -> new AvailableBackgroundResource(
            BackgroundType.PREVIOUS_CONSULTATION, consultation.consultationId(),
            consultation.date(), consultation.reason(), consultation.diagnosis()));
    var analyses = visionAnalysisQueryService.handle(new GetVisionAnalysesByPetIdQuery(petId))
        .stream()
        .filter(VisionAnalysis::isCompleted)
        .map(analysis -> new AvailableBackgroundResource(BackgroundType.VISION_ANALYSIS,
            analysis.getId(), analysis.analysisDate(), titleOf(analysis),
            analysis.findingsSummary()));
    return Stream.concat(consultations, analyses)
        .sorted(Comparator.comparing(AvailableBackgroundResource::date).reversed())
        .toList();
  }

  private static String titleOf(VisionAnalysis analysis) {
    return analysis.getImageType() == ImageType.RADIOGRAPH
        ? "Análisis de radiografía"
        : "Análisis visual de piel";
  }
}
