package com.nexoshift.petcarehub.iam.application.internal.commandservices;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.email.EmailService;
import com.nexoshift.petcarehub.iam.application.internal.outboundservices.hashing.HashingService;
import com.nexoshift.petcarehub.iam.domain.exceptions.AccountNotFoundException;
import com.nexoshift.petcarehub.iam.domain.exceptions.InvalidPasswordResetTokenException;
import com.nexoshift.petcarehub.iam.domain.exceptions.UserNotFoundException;
import com.nexoshift.petcarehub.iam.domain.model.aggregates.PasswordResetToken;
import com.nexoshift.petcarehub.iam.domain.model.commands.ChangePasswordCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.ResetPasswordCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.PasswordHash;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.iam.domain.repositories.AccountRepository;
import com.nexoshift.petcarehub.iam.domain.repositories.PasswordResetTokenRepository;
import com.nexoshift.petcarehub.iam.domain.repositories.UserRepository;
import com.nexoshift.petcarehub.iam.domain.services.PasswordCommandService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordCommandServiceImpl implements PasswordCommandService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final HashingService hashingService;
  private final EmailService emailService;
  private final Duration tokenValidity;

  public PasswordCommandServiceImpl(UserRepository userRepository,
      AccountRepository accountRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      HashingService hashingService, EmailService emailService,
      @Value("${iam.password-reset.token-validity-minutes:30}") long tokenValidityMinutes) {
    this.userRepository = userRepository;
    this.accountRepository = accountRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.hashingService = hashingService;
    this.emailService = emailService;
    this.tokenValidity = Duration.ofMinutes(tokenValidityMinutes);
  }

  /** Always ends silently, so nobody can find out which e-mails are registered. */
  @Override
  @Transactional
  public void handle(RequestPasswordRecoveryCommand command) {
    userRepository.findByEmailAddress(command.email().trim().toLowerCase())
        .filter(User::isActive)
        .ifPresent(user -> {
          var resetToken = passwordResetTokenRepository.save(
              PasswordResetToken.issue(new UserId(user.getId()), tokenValidity));
          emailService.sendPasswordRecoveryEmail(user.getEmailAddress(), resetToken.getToken());
        });
  }

  @Override
  @Transactional
  public void handle(ResetPasswordCommand command) {
    var resetToken = passwordResetTokenRepository.findByToken(command.token())
        .filter(PasswordResetToken::isUsable)
        .orElseThrow(InvalidPasswordResetTokenException::new);
    var userId = resetToken.getUserId().userId();
    var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    updatePassword(user, command.newPassword());
    resetToken.markAsUsed();
    passwordResetTokenRepository.save(resetToken);
  }

  @Override
  @Transactional
  public void handle(ChangePasswordCommand command) {
    var user = userRepository.findById(command.userId())
        .orElseThrow(() -> new UserNotFoundException(command.userId()));
    if (!hashingService.matches(command.currentPassword(), user.getPasswordHash().value())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }
    updatePassword(user, command.newPassword());
  }

  /** The password is changed through the Account aggregate that owns the user. */
  private void updatePassword(User user, String newPassword) {
    var account = accountRepository.findById(user.getAccountId())
        .orElseThrow(() -> new AccountNotFoundException(user.getAccountId()));
    var managedUser = account.getUsers().stream()
        .filter(item -> item.getId().equals(user.getId()))
        .findFirst()
        .orElseThrow(() -> new UserNotFoundException(user.getId()));
    managedUser.changePassword(new PasswordHash(hashingService.encode(newPassword)));
    accountRepository.save(account);
  }
}
