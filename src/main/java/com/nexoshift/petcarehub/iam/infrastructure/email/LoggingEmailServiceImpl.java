package com.nexoshift.petcarehub.iam.infrastructure.email;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Development implementation: writes the recovery link in the log instead of sending it. */
@Service
@Profile("!prod")
public class LoggingEmailServiceImpl implements EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailServiceImpl.class);

  private final String passwordResetUrl;

  public LoggingEmailServiceImpl(@Value("${iam.password-reset.url}") String passwordResetUrl) {
    this.passwordResetUrl = passwordResetUrl;
  }

  @Override
  public void sendPasswordRecoveryEmail(String recipient, String resetToken) {
    LOGGER.info("[DEV] Password recovery e-mail for {}: {}?token={}",
        recipient, passwordResetUrl, resetToken);
  }
}
