package com.nexoshift.petcarehub.iam.domain.repositories;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import java.util.Optional;

/** Persistence port of the Account aggregate. Implemented in infrastructure. */
public interface AccountRepository {

  Account save(Account account);

  Optional<Account> findById(Long id);
}
