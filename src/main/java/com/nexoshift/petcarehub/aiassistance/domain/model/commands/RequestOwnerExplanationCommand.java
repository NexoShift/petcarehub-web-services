package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

/** A question of the pet owner about their pet; consultationId is an optional reference. */
public record RequestOwnerExplanationCommand(Long petId, Long consultationId, Long ownerUserId,
    String question) {

  public static final int MAX_QUESTION_LENGTH = 1000;

  public RequestOwnerExplanationCommand {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id is required");
    }
    if (question == null || question.isBlank()) {
      throw new IllegalArgumentException("Question is required");
    }
    question = question.trim();
    if (question.length() > MAX_QUESTION_LENGTH) {
      throw new IllegalArgumentException(
          "The question must have at most " + MAX_QUESTION_LENGTH + " characters");
    }
  }
}
