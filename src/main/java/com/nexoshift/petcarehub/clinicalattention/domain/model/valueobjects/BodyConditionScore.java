package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Body condition score of the pet, from 1 (very thin) to 9 (obese). */
@Embeddable
public record BodyConditionScore(@Column(name = "body_condition_score") Integer score) {

  public BodyConditionScore {
    if (score == null || score < 1 || score > 9) {
      throw new IllegalArgumentException("Body condition score must be between 1 and 9");
    }
  }
}
