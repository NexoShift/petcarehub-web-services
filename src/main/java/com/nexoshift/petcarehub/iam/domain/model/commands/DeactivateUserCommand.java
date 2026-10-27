package com.nexoshift.petcarehub.iam.domain.model.commands;

/** requesterUserId is the authenticated user; it must be the holder of the same account. */
public record DeactivateUserCommand(Long userId, Long requesterUserId) {

  public DeactivateUserCommand {
    if (userId == null || userId <= 0 || requesterUserId == null || requesterUserId <= 0) {
      throw new IllegalArgumentException("User id and requester id are required");
    }
  }
}
