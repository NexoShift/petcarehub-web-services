package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

/** Maximum lengths of the free text fields. They are shared by the domain and the API. */
public final class ClinicalFieldLimits {

  public static final int REASON = 350;

  public static final int CHIEF_COMPLAINT = 350;
  public static final int EVOLUTION_TIME = 100;
  public static final int SYMPTOM = 100;
  public static final int MAX_SYMPTOMS = 20;
  public static final int DIET = 500;
  public static final int BEHAVIOR = 500;
  public static final int PREVIOUS_TREATMENTS = 1000;
  public static final int OWNER_OBSERVATIONS = 1000;

  public static final int MUCOUS_MEMBRANES = 100;
  public static final int ASSESSMENT_OBSERVATIONS = 2000;

  public static final int FINDING_DESCRIPTION = 1000;

  public static final int DIAGNOSIS_DESCRIPTION = 500;
  public static final int DIFFERENTIAL_DIAGNOSIS = 200;
  public static final int MAX_DIFFERENTIAL_DIAGNOSES = 10;
  public static final int RATIONALE = 2000;

  public static final int CANCELLATION_REASON = 500;

  private ClinicalFieldLimits() {
  }
}
