package com.nexoshift.petcarehub.iam.domain.services;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByEmailQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUsersByAccountIdQuery;

import java.util.List;
import java.util.Optional;

public interface AccountQueryService {

  Optional<User> handle(GetUserByIdQuery query);

  Optional<User> handle(GetUserByEmailQuery query);

  Optional<Account> handle(GetAccountByIdQuery query);

  /**
   * Retrieves all users belonging to an account.
   *
   * @param query the account users query
   * @return the account users
   */
  List<User> handle(GetUsersByAccountIdQuery query);

}
