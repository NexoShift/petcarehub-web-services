package com.nexoshift.petcarehub.aiassistance.application;

import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.commandservices.VisionAnalysisCommandServiceImpl;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StoredFile;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult.Detection;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelService;
import com.nexoshift.petcarehub.aiassistance.application.internal.validation.ImageValidator;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ImageNotEvaluableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.AiAnalysisStatus;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import com.nexoshift.petcarehub.aiassistance.support.TestImages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Caso de uso "subir imagen y analizarla", sin Spring: los puertos se simulan con Mockito.
 * Umbrales: piel 0.50, radiografía 0.20 (los mismos de application.properties).
 */
class VisionAnalysisCommandServiceImplTest {

  private static final long PET_ID = 5L;
  private static final long VET_USER_ID = 10L;

  private VisionModelService visionModel;
  private FileStorageService storage;
  private ExternalSubscriptionsService subscriptions;
  private final List<AiAnalysisStatus> savedStatuses = new ArrayList<>();
  private VisionAnalysisCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    var repository = mock(VisionAnalysisRepository.class);
    when(repository.save(any())).thenAnswer(call -> {
      VisionAnalysis analysis = call.getArgument(0);
      savedStatuses.add(analysis.getStatus());
      return analysis;
    });
    visionModel = mock(VisionModelService.class);
    storage = mock(FileStorageService.class);
    when(storage.store(any(), anyString(), anyString()))
        .thenReturn(new StoredFile("vision/2026/11/x.png", "image/png", 100L));
    var profiles = mock(ExternalProfilesService.class);
    when(profiles.fetchPetSummary(PET_ID)).thenReturn(Optional.of(
        new PetSummary(PET_ID, "Spot", "Perro", "Mestizo", "macho", 26, "marrón")));
    when(profiles.isPetPatientOfVeterinarianClinic(PET_ID, VET_USER_ID)).thenReturn(true);
    subscriptions = mock(ExternalSubscriptionsService.class);
    service = new VisionAnalysisCommandServiceImpl(repository, visionModel, storage,
        new ImageValidator(), profiles, subscriptions,
        mock(ExternalClinicalAttentionService.class), new PetAccessGuard(profiles), 0.50, 0.20);
  }

  private static RequestVisionAnalysisCommand command(ImageType type, byte[] image) {
    return new RequestVisionAnalysisCommand(null, PET_ID, VET_USER_ID, RequesterRole.VETERINARIAN,
        type, image, "image/png");
  }

  @Test
  @DisplayName("A skin photo is stored, analyzed and saved COMPLETED with the probable findings")
  void analyzesSkin() {
    when(visionModel.detect(any(), anyString(), any())).thenReturn(new VisionModelResult("piel/1",
        List.of(new Detection("dermatitis", 0.82, 0.3, 0.25, 0.35, 0.3),
            new Detection("micosis", 0.31, 0.6, 0.6, 0.1, 0.1))));

    var analysis = service.handle(command(ImageType.SKIN, TestImages.skin(300, 300)));

    assertThat(analysis.getStatus()).isEqualTo(AiAnalysisStatus.COMPLETED);
    assertThat(analysis.getFindings()).extracting("label").containsExactly("dermatitis");
    assertThat(analysis.getModelId()).isEqualTo("piel/1");
    assertThat(savedStatuses).containsExactly(AiAnalysisStatus.PROCESSING, AiAnalysisStatus.COMPLETED);
    verify(subscriptions).ensureAiUsageAllowed(VET_USER_ID);
  }

  @Test
  @DisplayName("Radiographs use the lower threshold (0.20)")
  void radiographThreshold() {
    when(visionModel.detect(any(), anyString(), any())).thenReturn(new VisionModelResult("rx/1",
        List.of(new Detection("efusion_pleural", 0.41, null, null, null, null),
            new Detection("normal", 0.12, null, null, null, null))));

    var analysis = service.handle(command(ImageType.RADIOGRAPH, TestImages.radiograph(300, 300)));

    assertThat(analysis.getFindings()).extracting("label").containsExactly("efusion_pleural");
  }

  @Test
  @DisplayName("If the provider is down the analysis is saved FAILED and the error is 503")
  void providerDown() {
    when(visionModel.detect(any(), anyString(), any()))
        .thenThrow(new AiProviderUnavailableException("roboflow", new RuntimeException("timeout")));

    assertThatThrownBy(() -> service.handle(command(ImageType.SKIN, TestImages.skin(300, 300))))
        .isInstanceOf(AiProviderUnavailableException.class);
    assertThat(savedStatuses).containsExactly(AiAnalysisStatus.PROCESSING, AiAnalysisStatus.FAILED);
  }

  @Test
  @DisplayName("An unusable prediction from the provider is also saved FAILED (503)")
  void badPrediction() {
    when(visionModel.detect(any(), anyString(), any())).thenReturn(new VisionModelResult("piel/1",
        List.of(new Detection("dermatitis", 0.9, null, null, null, null))));   // sin caja

    assertThatThrownBy(() -> service.handle(command(ImageType.SKIN, TestImages.skin(300, 300))))
        .isInstanceOf(AiProviderUnavailableException.class);
    assertThat(savedStatuses).last().isEqualTo(AiAnalysisStatus.FAILED);
  }

  @Test
  @DisplayName("An image that cannot be evaluated is rejected before storing it or calling the model")
  void invalidImageSpendsNothing() {
    assertThatThrownBy(() -> service.handle(command(ImageType.SKIN, TestImages.skin(100, 100))))
        .isInstanceOf(ImageNotEvaluableException.class);

    verify(storage, never()).store(any(), anyString(), anyString());
    verify(visionModel, never()).detect(any(), anyString(), any());
    verify(subscriptions, never()).ensureAiUsageAllowed(any());
  }
}
