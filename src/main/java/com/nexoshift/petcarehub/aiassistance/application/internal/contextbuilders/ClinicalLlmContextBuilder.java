package com.nexoshift.petcarehub.aiassistance.application.internal.contextbuilders;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Turns the ids the veterinarian selected into the text the model will read.
 * Every background must belong to the same pet: otherwise a consultation of another
 * clinic could leak to the LLM.
 */
@Component
public class ClinicalLlmContextBuilder {

  private final ExternalProfilesService profilesService;
  private final ExternalClinicalAttentionService clinicalAttentionService;
  private final VisionAnalysisRepository visionAnalysisRepository;

  public ClinicalLlmContextBuilder(ExternalProfilesService profilesService,
      ExternalClinicalAttentionService clinicalAttentionService,
      VisionAnalysisRepository visionAnalysisRepository) {
    this.profilesService = profilesService;
    this.clinicalAttentionService = clinicalAttentionService;
    this.visionAnalysisRepository = visionAnalysisRepository;
  }

  public ClinicalLlmContext build(Long petId, String currentReason,
      List<SelectedBackground> selectedBackgrounds) {
    var pet = profilesService.fetchPetSummary(petId)
        .orElseThrow(() -> new NotFoundException("Pet " + petId + " was not found"));
    var backgrounds = selectedBackgrounds.stream()
        .map(background -> describe(petId, background))
        .toList();
    return new ClinicalLlmContext(pet.toPromptText(), currentReason, backgrounds);
  }

  private String describe(Long petId, SelectedBackground background) {
    return switch (background.type()) {
      case PREVIOUS_CONSULTATION -> clinicalAttentionService
          .fetchConsultation(background.referenceId())
          .filter(consultation -> consultation.petId().equals(petId))
          .map(ConsultationBackground::toPromptText)
          .orElseThrow(() -> new BadRequestException(
              "Consultation " + background.referenceId() + " is not a background of this pet"));
      // Same module, so the repository is used directly (no ACL needed).
      case VISION_ANALYSIS -> visionAnalysisRepository.findById(background.referenceId())
          .filter(analysis -> analysis.getPetId().equals(petId))
          .filter(VisionAnalysis::isCompleted)
          .map(VisionAnalysis::toBackgroundText)
          .orElseThrow(() -> new BadRequestException(
              "Vision analysis " + background.referenceId() + " is not a background of this pet"));
    };
  }
}
