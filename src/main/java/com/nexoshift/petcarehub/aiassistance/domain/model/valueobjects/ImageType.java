package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

/** Chosen by the veterinarian before uploading. Pet owners can only upload SKIN. */
public enum ImageType {
  /** Skin or external lesion photo: object detection, findings with a box. */
  SKIN,
  /** Radiograph: classification, findings without a box. */
  RADIOGRAPH
}
