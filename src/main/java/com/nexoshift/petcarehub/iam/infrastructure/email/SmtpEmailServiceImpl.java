package com.nexoshift.petcarehub.iam.infrastructure.email;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends real e-mails through Gmail SMTP (production profile). */
@Service
@Profile("prod")
public class SmtpEmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final String passwordResetUrl;
  private final String sender;

  public SmtpEmailServiceImpl(JavaMailSender mailSender,
      @Value("${iam.password-reset.url}") String passwordResetUrl,
      @Value("${spring.mail.username}") String sender) {
    this.mailSender = mailSender;
    this.passwordResetUrl = passwordResetUrl;
    this.sender = sender;
  }

  @Override
  public void sendPasswordRecoveryEmail(String recipient, String resetToken) {
    var message = new SimpleMailMessage();
    message.setFrom(sender);
    message.setTo(recipient);
    message.setSubject("PetCare Hub - Recupera tu contraseña");
    message.setText("Hola,\n\nPara crear una nueva contraseña entra a este enlace:\n"
        + passwordResetUrl + "?token=" + resetToken
        + "\n\nSi no lo solicitaste, ignora este correo.\n\nEquipo PetCare Hub");
    mailSender.send(message);
  }
}
