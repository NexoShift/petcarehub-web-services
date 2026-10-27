package com.nexoshift.petcarehub.iam.domain.model.aggregates;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Temporary, single-use token sent by e-mail to reset a password (US31). */
@Entity
public class PasswordResetToken extends AuditableAbstractAggregateRoot<PasswordResetToken> {

  @Embedded
  @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false))
  private UserId userId;

  @Column(nullable = false, unique = true, length = 64)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean used;

  protected PasswordResetToken() {
  }

  private PasswordResetToken(UserId userId, String token, Instant expiresAt) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
    this.used = false;
  }

  public static PasswordResetToken issue(UserId userId, Duration validity) {
    if (validity == null || validity.isNegative() || validity.isZero()) {
      throw new IllegalArgumentException("Token validity must be positive");
    }
    return new PasswordResetToken(userId, UUID.randomUUID().toString(), Instant.now().plus(validity));
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isUsable() {
    return !used && !isExpired();
  }

  public void markAsUsed() {
    if (!isUsable()) {
      throw new IllegalStateException("Password reset token is expired or already used");
    }
    this.used = true;
  }

  public UserId getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isUsed() {
    return used;
  }
}
