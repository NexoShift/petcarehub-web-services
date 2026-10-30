package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Id of a consulting room. Rooms belong to a clinic in Profiles & Organization. */
@Embeddable
public record ConsultingRoomId(Long consultingRoomId) {

  public ConsultingRoomId {
    if (consultingRoomId == null || consultingRoomId <= 0) {
      throw new IllegalArgumentException("Consulting room id must be greater than zero");
    }
  }
}
