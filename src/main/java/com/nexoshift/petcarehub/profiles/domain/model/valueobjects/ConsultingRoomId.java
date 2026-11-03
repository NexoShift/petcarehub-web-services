package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

public record ConsultingRoomId(Long consultingRoomId) {

  public ConsultingRoomId {
    if (consultingRoomId == null || consultingRoomId <= 0) {
      throw new IllegalArgumentException("Consulting Room ID must be greater than zero");
    }
  }
}
