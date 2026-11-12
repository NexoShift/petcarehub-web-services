package com.nexoshift.petcarehub.iam.application.internal.queryservices;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByEmailQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUsersByAccountIdQuery;
import com.nexoshift.petcarehub.iam.domain.repositories.AccountRepository;
import com.nexoshift.petcarehub.iam.domain.repositories.UserRepository;
import com.nexoshift.petcarehub.iam.domain.services.AccountQueryService;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountQueryServiceImpl implements AccountQueryService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;

  public AccountQueryServiceImpl(AccountRepository accountRepository,
      UserRepository userRepository) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Optional<User> handle(GetUserByIdQuery query) {
    return userRepository.findById(query.userId());
  }

  @Override
  public Optional<User> handle(GetUserByEmailQuery query) {
    if (query.email() == null) {
      return Optional.empty();
    }
    return userRepository.findByEmailAddress(query.email().trim().toLowerCase());
  }

  @Override
  public Optional<Account> handle(GetAccountByIdQuery query) {
    return accountRepository.findById(query.accountId());
  }



  /**
   * Retrieves all users belonging to an IAM account.
   *
   * @param query the account users query
   * @return the account users
   */
  @Override
  public List<User> handle(GetUsersByAccountIdQuery query) {
    return userRepository.findByAccountId(query.accountId());
  }


}
