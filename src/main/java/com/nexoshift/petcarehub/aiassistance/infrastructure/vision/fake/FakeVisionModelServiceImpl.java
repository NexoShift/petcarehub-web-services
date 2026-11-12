package com.nexoshift.petcarehub.aiassistance.infrastructure.vision.fake;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult.Detection;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelService;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Offline vision model for development, tests and as a demo fallback. It always answers the
 * same, with the real class names. The low-confidence predictions are there to show that
 * the domain discards them (skin under 50 %, radiograph under 20 %).
 */
@Service
@ConditionalOnProperty(name = "ai.vision.provider", havingValue = "fake", matchIfMissing = true)
public class FakeVisionModelServiceImpl implements VisionModelService {

  @Override
  public VisionModelResult detect(byte[] image, String contentType, ImageType imageType) {
    if (imageType == ImageType.RADIOGRAPH) {
      return new VisionModelResult("fake-radiografias/1", List.of(
          new Detection("cardiomegalia", 0.78, null, null, null, null),
          new Detection("efusion_pleural", 0.41, null, null, null, null),
          new Detection("normal", 0.12, null, null, null, null)));
    }
    return new VisionModelResult("fake-piel/1", List.of(
        new Detection("dermatitis", 0.82, 0.30, 0.25, 0.35, 0.30),
        new Detection("micosis", 0.31, 0.65, 0.60, 0.15, 0.15)));
  }
}
