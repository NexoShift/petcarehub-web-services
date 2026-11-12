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
import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelService;
import com.nexoshift.petcarehub.aiassistance.application.internal.validation.ImageValidator;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
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
  private final ExternalClinicalAttentionService clinicalAttentionService;
  private final PetAccessGuard petAccessGuard;
  private final double skinMinConfidence;
  private final double radiographMinConfidence;

  public VisionAnalysisCommandServiceImpl(VisionAnalysisRepository repository,
                                          VisionModelService visionModelService, FileStorageService fileStorageService,
                                          ImageValidator imageValidator, ExternalProfilesService profilesService,
                                          ExternalSubscriptionsService subscriptionsService,
                                          ExternalClinicalAttentionService clinicalAttentionService, PetAccessGuard petAccessGuard,
                                          @Value("${ai.vision.skin-min-confidence:0.50}") double skinMinConfidence,
                                          @Value("${ai.vision.radiograph-min-confidence:0.20}") double radiographMinConfidence) {
    this.repository = repository;
    this.visionModelService = visionModelService;
    this.fileStorageService = fileStorageService;
    this.imageValidator = imageValidator;
    this.profilesService = profilesService;
    this.subscriptionsService = subscriptionsService;
    this.clinicalAttentionService = clinicalAttentionService;
    this.petAccessGuard = petAccessGuard;
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

  /**
   * Validaciones antes de gastar almacenamiento o créditos del modelo:
   * 1) la mascota existe (404);
   * 2) el usuario puede usarla: dueño de la mascota, o vet de una clínica donde es paciente (403);
   * 3) NUEVO: si viene consultationId (solo vets), la consulta existe (404), es de la misma
   *    mascota (400) y está en curso (409).
   */
  private void ensurePetCanBeAnalyzed(RequestVisionAnalysisCommand command) {
    if (profilesService.fetchPetSummary(command.petId()).isEmpty()) {
      throw new NotFoundException("Pet " + command.petId() + " was not found");
    }
    petAccessGuard.ensureCanAccessPet(
            command.petId(), command.requesterId(), command.requesterRole());
    if (command.consultationId() != null) {
      var petOfConsultation = clinicalAttentionService
              .fetchPetIdByConsultationId(command.consultationId())
              .orElseThrow(() -> new NotFoundException(
                      "Clinical consultation " + command.consultationId() + " was not found"));
      if (!petOfConsultation.equals(command.petId())) {
        throw new BadRequestException("The clinical consultation belongs to another pet");
      }
      if (!clinicalAttentionService.isConsultationInProgress(command.consultationId())) {
        throw new ConflictException("The clinical consultation must be in progress");
      }
    }
  }
}
