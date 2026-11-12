package com.nexoshift.petcarehub.iam.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.repositories.AccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data adapter of the AccountRepository port. */
@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long>, AccountRepository {
}
