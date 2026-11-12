package com.nexoshift.petcarehub.iam.domain.services;

import com.nexoshift.petcarehub.iam.domain.model.commands.ChangePasswordCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.ResetPasswordCommand;

public interface PasswordCommandService {

  void handle(RequestPasswordRecoveryCommand command);

  void handle(ResetPasswordCommand command);

  void handle(ChangePasswordCommand command);
}
