package com.nexoshift.petcarehub.iam.domain.services;

import com.nexoshift.petcarehub.iam.domain.model.commands.SignInWithGoogleCommand;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.GoogleSession;

import com.nexoshift.petcarehub.iam.domain.model.commands.ActivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.CreateMemberUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.DeactivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.SignInCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.SignUpCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AuthenticatedSession;
import java.util.Optional;

public interface AccountCommandService {

  /** Registers a new account and returns its holder user. */
  Optional<User> handle(SignUpCommand command);

  Optional<AuthenticatedSession> handle(SignInCommand command);

  Optional<User> handle(CreateMemberUserCommand command);

  Optional<User> handle(ActivateUserCommand command);

  Optional<User> handle(DeactivateUserCommand command);

  /**
   * Sign in with Google. If no user has that email yet, the account is created with
   * {@code accountType}; without it the answer is 422 so the frontend can ask for it.
   */
  GoogleSession handle(SignInWithGoogleCommand command);
}
