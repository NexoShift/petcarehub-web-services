package com.nexoshift.petcarehub.iam.application.internal.outboundservices.email;

/** Port: outgoing e-mail. SMTP in production, log in development. */
public interface EmailService {

  void sendPasswordRecoveryEmail(String recipient, String resetToken);
}
