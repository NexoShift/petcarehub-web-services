package com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects;

/** Maximum lengths of the free text fields, shared by the domain and the API. */
public final class MedicalHistoryFieldLimits {

  public static final int ALLERGEN = 150;
  public static final int REACTION = 500;
  public static final int NOTES = 1000;
  public static final int VACCINE_NAME = 150;
  public static final int BATCH_NUMBER = 50;
  public static final int MAX_DOSE_NUMBER = 20;
  public static final int ENTRY_SUMMARY = 500;
  public static final int EXAM_TYPE = 30;

  private MedicalHistoryFieldLimits() {
  }
}
