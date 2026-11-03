package com.nexoshift.petcarehub.aiassistance.application.internal.commandservices;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BoundingBox;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ConfidenceScore;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.VisionAnalysisCommandService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelService;
import com.nexoshift.petcarehub.aiassistance.application.internal.validation.ImageValidator;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VisionAnalysisCommandServiceImpl implements VisionAnalysisCommandService {

  private static final String FOLDER = "vision";

  private final VisionAnalysisRepository repository;
  private final VisionModelService visionModelService;
  private final FileStorageService fileStorageService;
  private final ImageValidator imageValidator;
  private final ExternalProfilesService profilesService;
  private final ExternalSubscriptionsService subscriptionsService;
  private final double skinMinConfidence;
  private final double radiographMinConfidence;

  public VisionAnalysisCommandServiceImpl(VisionAnalysisRepository repository,
      VisionModelService visionModelService, FileStorageService fileStorageService,
      ImageValidator imageValidator, ExternalProfilesService profilesService,
      ExternalSubscriptionsService subscriptionsService,
      @Value("${ai.vision.skin-min-confidence:0.50}") double skinMinConfidence,
      @Value("${ai.vision.radiograph-min-confidence:0.20}") double radiographMinConfidence) {
    this.repository = repository;
    this.visionModelService = visionModelService;
    this.fileStorageService = fileStorageService;
    this.imageValidator = imageValidator;
    this.profilesService = profilesService;
    this.subscriptionsService = subscriptionsService;
    this.skinMinConfidence = skinMinConfidence;
    this.radiographMinConfidence = radiographMinConfidence;
  }

  /*
   * No @Transactional here on purpose, as in the LLM: the model call can take up to 8 s,
   * and if the provider fails the FAILED status must stay saved.
   */
  @Override
  public VisionAnalysis handle(RequestVisionAnalysisCommand command) {
    ensurePetCanBeAnalyzed(command);
    imageValidator.validate(command.image(), command.imageType());   // 422 before spending
    subscriptionsService.ensureAiUsageAllowed(command.requesterId());

    var stored = fileStorageService.store(command.image(), command.contentType(), FOLDER);
    var image = new ImageReference(stored.storageKey(), stored.contentType(), stored.sizeBytes());

    var analysis = new VisionAnalysis(command, image);
    analysis.startProcessing();
    analysis = repository.save(analysis);
    try {
      var result = visionModelService.detect(
          command.image(), command.contentType(), command.imageType());
      var threshold = command.imageType() == ImageType.RADIOGRAPH
          ? radiographMinConfidence : skinMinConfidence;
      for (var detection : result.detections()) {
        var box = detection.hasLocation()
            ? new BoundingBox(detection.left(), detection.top(), detection.width(),
                detection.height())
            : null;
        analysis.recordFinding(detection.label(), new ConfidenceScore(detection.confidence()),
            box, threshold);
      }
      analysis.complete(result.modelId());
      return repository.save(analysis);
    } catch (AiProviderUnavailableException | IllegalArgumentException exception) {
      // IllegalArgumentException: the provider returned a prediction we cannot use.
      analysis.fail(exception.getMessage());
      repository.save(analysis);
      throw exception instanceof AiProviderUnavailableException unavailable
          ? unavailable
          : new AiProviderUnavailableException("vision", exception);
    }
  }

  private void ensurePetCanBeAnalyzed(RequestVisionAnalysisCommand command) {
    if (profilesService.fetchPetSummary(command.petId()).isEmpty()) {
      throw new NotFoundException("Pet " + command.petId() + " was not found");
    }
    if (command.requesterRole() == RequesterRole.PET_OWNER
        && !profilesService.isPetOwnedBy(command.petId(), command.requesterId())) {
      throw new ForbiddenException("You can only analyze images of your own pets");
    }
  }
}
