package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;

/** Port to the computer vision model. Implemented by the fake and the Roboflow adapters. */
public interface VisionModelService {

  /**
   * Analyzes the image with the model of its type: detection for SKIN, classification for
   * RADIOGRAPH. Returns every prediction; the domain decides which ones are reliable.
   *
   * @throws AiProviderUnavailableException if the provider fails or exceeds the timeout
   */
  VisionModelResult detect(byte[] image, String contentType, ImageType imageType);
}
