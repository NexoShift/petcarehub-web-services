package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import java.util.Set;

/** consultationId is optional: pet owners upload outside a consultation, so it is dropped for them. */
public record RequestVisionAnalysisCommand(
    Long consultationId,
    Long petId,
    Long requesterId,
    RequesterRole requesterRole,
    ImageType imageType,
    byte[] image,
    String contentType) {

  public static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

  public RequestVisionAnalysisCommand {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id is required");
    }
    if (requesterRole == null) {
      throw new IllegalArgumentException("Requester role is required");
    }
    if (imageType == null) {
      throw new IllegalArgumentException("Image type is required: SKIN or RADIOGRAPH");
    }
    if (requesterRole == RequesterRole.PET_OWNER && imageType != ImageType.SKIN) {
      throw new ForbiddenException("Pet owners can only upload skin photos");
    }
    if (image == null || image.length == 0) {
      throw new IllegalArgumentException("Image is required");
    }
    if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
      throw new IllegalArgumentException("Only JPG and PNG images are accepted");
    }
    contentType = contentType.toLowerCase();
    if (requesterRole == RequesterRole.PET_OWNER) {
      consultationId = null;
    }
  }
}
