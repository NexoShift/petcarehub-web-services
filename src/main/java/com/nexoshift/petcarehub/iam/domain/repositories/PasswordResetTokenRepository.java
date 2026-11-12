package com.nexoshift.petcarehub.iam.domain.repositories;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetTokenRepository {

  PasswordResetToken save(PasswordResetToken passwordResetToken);

  Optional<PasswordResetToken> findByToken(String token);
}
