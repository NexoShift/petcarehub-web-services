package com.nexoshift.petcarehub.iam.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.PasswordResetToken;
import com.nexoshift.petcarehub.iam.domain.repositories.PasswordResetTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data adapter of the PasswordResetTokenRepository port. */
@Repository
public interface PasswordResetTokenJpaRepository
    extends JpaRepository<PasswordResetToken, Long>, PasswordResetTokenRepository {
}
